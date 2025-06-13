package example.micronaut

import com.fasterxml.jackson.databind.JsonNode
import com.slack.api.Slack
import com.slack.api.methods.SlackApiException
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import example.micronaut.config.ConfigProperties
import example.micronaut.models.ApiRequest
import example.micronaut.models.ApiResponse
import example.micronaut.models.Message
import example.micronaut.models.SearchRequest
import example.micronaut.models.SearchResponse
import io.micronaut.scheduling.TaskExecutors
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService

@Singleton
class SlackMessageProcessor(
  private val apiClient: ApiClient,
  private val configProperties: ConfigProperties,
  @Named(TaskExecutors.IO) private val executorService: ExecutorService
) {
  companion object {
    private const val APP = "knowwheretogo"
  }

  val sampleQnA = mapOf(
    "Who should I contact for Concur-related issues like returned claims?"
      to
      "Please reach out to @Stephy.Sabu for any Concur-related queries.",

    "My expense claim has been in 'Extracted for Payment' status for over a month. What should I do?"
      to
      "Email CorporateHR.services@target.com to get assistance.",

    "Should I submit all my bills together for reimbursement if I made purchases on the same day?"
      to
      "Yes, it's recommended to submit all bills together if the purchases were made on the same day.",

    "Who should I contact for medical or personal accident insurance queries?"
      to
      "Email TargetIndia.Pay&Benefits@target.com or contact Mathews P Samuel (Mathews.P@target.com, +91 7411022691).",

    "Where and when is Mathews P Samuel available for insurance queries?"
      to
      "Monday–Wednesday: C2, 7th floor, B wing, B7100 \n Thursday: NXT, 5th floor, room N511 Timing: 10 a.m. to 4.30 p.m. (Lunch: 1.30–2.00 p.m.)",

    "Where can I check network hospitals for medical insurance?"
      to
      "Use either of these links: \nPrudent Plus Network \n MediAssist TPA Network",

    "Where can I ask questions about the TAP program?"
      to
      "You can reach out to @Sanket.Darji or post in #tap-community.",

    "Where can I find help for Vella-related queries?"
      to
      "Join #vela-community on Slack.",

    "Is there a Slack channel for Sapphire support or questions?"
      to
      "Yes, use #sapphire for all Sapphire-related queries.",

    "Where can I access the iOS layered architecture guide?"
      to
      "Check here: https://pages.git.target.com/mobile-apps/guide/docs/flagship-ios/modules/module-landing/#layered-architecture",

    "What is the link for Firefly v2 and Firefly Insights?"
      to
      "Firefly v2: https://go/firefly-v2 \n Firefly Insights: https://go/firefly-insights",

    "Where can I access my payslips?"
      to
      "Log in to the Allsec portal: https://saml.iam.target.com/affwebservices/public/saml2sso?SPID=Allsec-ESS",

    "I have an issue with my Cult Elite subscription. Who should I contact?"
      to
      "Please contact Ekin care support via their website: https://app.ekincare.com/login",

    "Where can I find the Target India Virtual Work Reimbursement Policy for new hires?"
      to
      "You can access it here: https://targetonline.sharepoint.com/sites/hq-hub/SitePages/Target-in-India-Virtual-Work-Reimbursement-Program---New-Hires.aspx",

    "Whom should I contact for any iOS related query from Discretionary Journey Team"
      to
      "You can reach out to @Sanchit.Mehta",

    "Where can I ask for help with Redsky issues or doubts?"
      to
      "Post your queries in the Slack channel #redsky-support.",

    "Where should I request PR reviews for Redsky?"
      to
      "Use the Slack channel #redsky-self-service for PR review requests.",

    "Is there documentation available for Redsky?"
      to
      "Yes, check the Redsky documentation here: https://pages.git.target.com/Sapphire/docs/"
  )

  private val slack: Slack = Slack.getInstance()
  private val logger: Logger = LoggerFactory.getLogger(SlackMessageProcessor::class.java)

  fun processMessageAsync(event: JsonNode) {
    val dispatcher = executorService.asCoroutineDispatcher()
    CoroutineScope(dispatcher).launch {
      try {
        processMessage(event)
      } catch (e: Exception) {
        logger.error("Failed to process message", e)
      }
    }
  }

  private suspend fun processMessage(event: JsonNode) {
    val channel = event["channel"]?.asText()
    val text = event["text"]?.asText()

//    val replyText = searchInDbAndPrepareResponse(text.orEmpty())
    delay(2000)
    val replyText = sampleQnA[text.orEmpty()] ?: "Sorry, I couldn't find an answer to that question."

    try {
      val response: ChatPostMessageResponse = slack.methods(configProperties.slackBotToken).chatPostMessage {
        it.channel(channel).text(replyText)
      }
      if (!response.isOk) {
        logger.error("Slack API error: ${response.error}")
      }
    } catch (e: SlackApiException) {
      logger.error(e.message)
      e.printStackTrace()
    } catch (e: Exception) {
      logger.error(e.message)
    }
  }

  private suspend fun searchInDbAndPrepareResponse(query: String): String {
    val ragContext = makeSearchReq(query)

    val contents = ragContext?.documents?.joinToString("\n") { it.content } ?: ""
    val messages = listOf(
      Message("system", "The following is context pulled from a RAG system that may be needed to address the user's question: $contents"),
      Message("user", query)
    )

    val response = generateResponse(messages)
    return response?.choices?.firstOrNull()?.message?.content ?: "Sorry, I couldn't process that."
  }

  private suspend fun makeSearchReq(query: String): SearchResponse? {
    val searchRequest = SearchRequest(query = query)
    return try {
      apiClient.search(
        request = searchRequest,
        auth = "Bearer ${configProperties.thinktankBearerToken}",
        app = APP,
        tenantId = configProperties.thinktankTenantId
      )
    } catch (e: Exception) {
      logger.error(e.message)
      null
    }
  }

  private suspend fun generateResponse(messages: List<Message>): ApiResponse? {
    val request = ApiRequest(messages = messages)
    return try {
      apiClient.chatCompletion(
        request = request,
        auth = "Bearer ${configProperties.thinktankBearerToken}",
        app = APP
      )
    } catch (e: Exception) {
      logger.error(e.message)
      return null
    }
  }

}
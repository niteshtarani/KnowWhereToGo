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
    val user = event["user"]?.asText()
    val channel = event["channel"]?.asText()
    val text = event["text"]?.asText()

    val replyText = searchInDbAndPrepareResponse(text.orEmpty())

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
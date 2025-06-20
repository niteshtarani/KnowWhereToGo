package example.micronaut

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/slack/events")
class SlackBotController {

  @Inject
  lateinit var slackMessageProcessor: SlackMessageProcessor
  private val objectMapper = jacksonObjectMapper()
  private val logger: Logger = LoggerFactory.getLogger(SlackBotController::class.java)

  @Post(consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
  fun receiveSlackEvent(@Body rawBody: String): HttpResponse<Any> {
    logger.info("Received Slack event: $rawBody")
    val root = objectMapper.readTree(rawBody)

    // Handle Slack URL Verification challenge
    if (root["type"]?.asText() == "url_verification") {
      val challenge = root["challenge"]?.asText()
      return HttpResponse.ok(mapOf(Pair("challenge", challenge)))
    }

    // Handle event_callback
    if (root["type"]?.asText() == "event_callback") {
      val event = root["event"]

      // Ignore messages from bots
      if (event.has("bot_id") || event.has("subtype")) {
        return HttpResponse.ok()
      }

      val eventType = event["type"]?.asText()
      val channelType = event["channel_type"]?.asText()

      if (eventType == "message" && channelType == "im") {
        slackMessageProcessor.processMessageAsync(event)
      }
    }
    return HttpResponse.ok()
  }

}

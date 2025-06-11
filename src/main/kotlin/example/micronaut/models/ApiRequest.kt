package example.micronaut.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ApiRequest(
  val model: String = "gpt-4o-mini",
  val temperature: Int = 1,
  @JsonProperty("max_new_tokens")
  val maxNewTokens: Int = 2048,
  @JsonProperty("top_p")
  val topP: Int = 1,
  @JsonProperty("frequency_penalty")
  val frequencyPenalty: Double = 0.5,
  @JsonProperty("presence_penalty")
  val presencePenalty: Double = 0.0,
  val timeout: Int = 120,
  val stream: Boolean = false,
  val messages: List<Message>
)
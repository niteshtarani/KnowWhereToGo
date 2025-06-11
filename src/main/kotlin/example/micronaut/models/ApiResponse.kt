package example.micronaut.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ApiResponse(
  val choices: List<Choice>
) {
  @Serdeable
  data class Choice(val message: Message)
  @Serdeable
  data class Message(val role: String, val content: String)
}
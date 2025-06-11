package example.micronaut.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Message(
  val role: String,
  val content: String
)
package example.micronaut.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class SearchRequest(
  val query: String,
  val index: String = "knowwheretogo-onboarding",
  @JsonProperty("top_k")
  val topK: Int = 10,
  @JsonProperty("min_score")
  val minScore: Double = 0.5,
)
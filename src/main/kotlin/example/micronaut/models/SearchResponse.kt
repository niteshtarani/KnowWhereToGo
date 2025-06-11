package example.micronaut.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class SearchResponse(
  @JsonProperty("rag_documents")
  val documents: List<Document>,
) {
  @Serdeable
  data class Document(
    val content: String
  )
}

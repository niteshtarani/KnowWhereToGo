package example.micronaut

import example.micronaut.models.ApiRequest
import example.micronaut.models.ApiResponse
import example.micronaut.models.SearchRequest
import example.micronaut.models.SearchResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client

@Client("https://api-internal.target.com/")
interface ApiClient {

  @Post("gen_ai_model_requests/v1/chat/completions")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  suspend fun chatCompletion(
    @Body request: ApiRequest,
    @Header("Authorization") auth: String,
    @Header("X-TGT-APPLICATION") app: String
  ): ApiResponse

  @Post("gen_ai_rag_requests/v1/search")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  suspend fun search(
    @Body request: SearchRequest,
    @Header("Authorization") auth: String,
    @Header("X-TGT-APPLICATION") app: String,
    @Header("tenant-id") tenantId: String
  ): SearchResponse
}
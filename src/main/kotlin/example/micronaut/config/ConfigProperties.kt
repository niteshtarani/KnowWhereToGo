package example.micronaut.config

import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.inject.Singleton

@ConfigurationProperties("api.keys")
@Singleton
class ConfigProperties {
  lateinit var slackBotToken: String
  lateinit var thinktankTenantId: String
  lateinit var thinktankBearerToken: String
}
package gosex.backend

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureAuthentication() {
  val esiaHost = environment.config.property("gosex.esia.host").getString()
  val esiaPort = environment.config.property("gosex.esia.port").getString()
  val esiaBaseUrl = "http://$esiaHost:$esiaPort"

  val keycloakIssuer = "$esiaBaseUrl/realms/gosex"
  val keycloakAudience = "gosex-backend"
  val jwksUrl = "$esiaBaseUrl/realms/gosex/protocol/openid-connect/certs"
  val jwkProvider =
    JwkProviderBuilder(URL(jwksUrl))
      .cached(10, 24, TimeUnit.HOURS)
      .rateLimited(10, 1, TimeUnit.MINUTES)
      .build()

  install(Authentication) {
    jwt("auth-jwt") {
      realm = "gosex"
      verifier(jwkProvider, keycloakIssuer) {
        acceptLeeway(3)
        withAudience(keycloakAudience)
      }
      validate { credential ->
        if (credential.payload.getClaim("email_verified").asBoolean() == true) {
          JWTPrincipal(credential.payload)
        } else {
          null
        }
      }
    }
  }
}

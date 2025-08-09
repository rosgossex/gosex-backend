package gosex.backend

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureAuthentication() {
  val esiaRealm = "gosex"
  val esiaUrl = environment.config.property("gosex.esia.url").getString()
  val esiaIssuers = environment.config.property("gosex.esia.issuers").getString().split(",")

  val audience = "gosex-backend"
  val jwksUrl = "$esiaUrl/realms/$esiaRealm/protocol/openid-connect/certs"
  val jwkProvider =
    JwkProviderBuilder(URL(jwksUrl))
      .cached(10, 24, TimeUnit.HOURS)
      .rateLimited(10, 1, TimeUnit.MINUTES)
      .build()

  install(Authentication) {
    jwt("auth-jwt") {
      realm = esiaRealm

      verifier(jwkProvider) {
        acceptLeeway(3)
        withAudience(audience)
        withIssuer(*esiaIssuers.toTypedArray())
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

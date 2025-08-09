package gosex.backend

import gosex.backend.service.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(userService: UserService) {
  routing {
    authenticate("auth-jwt") {
      route("/users") {
        get("/search") {
          val nameQuery = call.request.queryParameters["q"]
          if (nameQuery.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing or empty 'q' query parameter")
            return@get
          }
          val users = userService.searchUsersByName(nameQuery)
          call.respond(HttpStatusCode.OK, users)
        }

        route("/me") {
          post {
            when (val result = userService.getUserOrRegister(call.principal<JWTPrincipal>())) {
              is APIResult.Error -> {
                call.respond(result.code, result.message)
                return@post
              }
              is APIResult.Success -> {
                val registrationResult = result.value
                val statusCode =
                  if (registrationResult.wasCreated) {
                    HttpStatusCode.Created
                  } else {
                    HttpStatusCode.OK
                  }
                call.respond(statusCode, registrationResult.user)
              }
            }
          }
        }
      }
    }
  }
}

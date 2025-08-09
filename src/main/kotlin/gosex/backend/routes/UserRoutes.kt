package gosex.backend.routes

import gosex.backend.resources.UsersResource
import gosex.backend.service.APIResult
import gosex.backend.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
  authenticate("auth-jwt") {
    get<UsersResource.Search> { resource ->
      if (resource.q.isBlank()) {
        call.respond(HttpStatusCode.BadRequest, "Missing or empty 'q' query parameter")
        return@get
      }
      val users = userService.searchUsersByName(resource.q)
      call.respond(HttpStatusCode.OK, users)
    }

    post<UsersResource.Me> { _ ->
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

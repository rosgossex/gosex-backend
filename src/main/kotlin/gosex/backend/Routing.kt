package gosex.backend

import gosex.backend.routes.userRoutes
import gosex.backend.service.UserService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(userService: UserService) {
  routing { userRoutes(userService) }
}

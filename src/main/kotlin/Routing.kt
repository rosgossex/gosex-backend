package gosex.backend

import gosex.backend.models.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Application.configureRouting() {
  routing {
    authenticate("auth-jwt") {
      route("/users") {
        get("/me") {
          call.respond(
              listOf(
                  User(
                      birthdate = LocalDate.parse("2010-06-01"),
                      gender = Gender.Male,
                      givenName = "Mikhail",
                      familyName = "Butvin")))
        }
      }
    }
  }
}

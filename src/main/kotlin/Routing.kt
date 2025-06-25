package gosex.backend

import gosex.backend.model.*
import gosex.backend.repo.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

sealed class APIResult<out T> {
  data class Success<out T>(val value: T) : APIResult<T>()

  data class Error(val code: HttpStatusCode, val message: String) : APIResult<Nothing>()
}

fun Application.configureRouting(userRepo: UserRepository) {
  routing {
    authenticate("auth-jwt") {
      route("/users") {
        get("/search") {
          val nameQuery = call.request.queryParameters["q"]
          if (nameQuery.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing or empty 'q' query parameter")
            return@get
          }
          val users = userRepo.usersByName(nameQuery)
          call.respond(HttpStatusCode.OK, users)
        }

        route("/me") {
          post {
            when (val result = userFromJwtPrincipal(call.principal<JWTPrincipal>())) {
              is APIResult.Error -> {
                call.respond(result.code, result.message)
                return@post
              }
              is APIResult.Success -> {
                val user = result.value
                val existingUser = userRepo.userById(user.id)
                if (existingUser != null) {
                  call.respond(HttpStatusCode.OK, existingUser)
                  return@post
                }

                if (user.age < 18) {
                  call.respond(HttpStatusCode.BadRequest, "User is underaged")
                  return@post
                }

                userRepo.addUser(user)
                call.respond(HttpStatusCode.Created, user)
              }
            }
          }
        }
      }
    }
  }
}

fun userFromJwtPrincipal(principal: JWTPrincipal?): APIResult<User> {
  if (principal == null) {
    return APIResult.Error(HttpStatusCode.BadRequest, "No JWT Principal")
  }
  val userId = principal.payload.getClaim("sub")?.asString()
  val givenName = principal.payload.getClaim("given_name")?.asString()
  val familyName = principal.payload.getClaim("family_name")?.asString()
  val birthdateString = principal.payload.getClaim("birthdate")?.asString()
  val genderString = principal.payload.getClaim("gender")?.asString()
  if (
    userId == null ||
      givenName == null ||
      familyName == null ||
      birthdateString == null ||
      genderString == null
  ) {
    return APIResult.Error(
      HttpStatusCode.BadRequest,
      "Missing required JWT claims: userId=$userId givenName=$givenName familyName=$familyName birthdateString=$birthdateString genderString=$genderString",
    )
  }

  val birthdate =
    try {
      LocalDate.parse(birthdateString)
    } catch (e: Exception) {
      return APIResult.Error(HttpStatusCode.BadRequest, "Invalid birthdate format in JWT")
    }

  val gender =
    when (genderString) {
      "male" -> Gender.Male
      "female" -> Gender.Female
      else -> return APIResult.Error(HttpStatusCode.BadRequest, "Invalid gender in JWT")
    }

  val user =
    User(
      id = userId,
      birthdate = birthdate,
      gender = gender,
      givenName = givenName,
      familyName = familyName,
    )
  return APIResult.Success(user)
}

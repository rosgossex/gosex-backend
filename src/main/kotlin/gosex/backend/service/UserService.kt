package gosex.backend.service

import gosex.backend.model.*
import gosex.backend.repo.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.*
import kotlinx.datetime.LocalDate

sealed class APIResult<out T> {
  data class Success<out T>(val value: T) : APIResult<T>()

  data class Error(val code: HttpStatusCode, val message: String) : APIResult<Nothing>()
}

data class UserRegistrationResult(val user: User, val wasCreated: Boolean)

class UserService(private val userRepository: UserRepository) {

  suspend fun searchUsersByName(nameQuery: String): List<User> {
    return userRepository.usersByName(nameQuery)
  }

  suspend fun getUserOrRegister(jwtPrincipal: JWTPrincipal?): APIResult<UserRegistrationResult> {
    when (val result = userFromJwtPrincipal(jwtPrincipal)) {
      is APIResult.Error -> return result
      is APIResult.Success -> {
        val user = result.value
        val existingUser = userRepository.userById(user.id)
        if (existingUser != null) {
          return APIResult.Success(UserRegistrationResult(existingUser, false))
        }

        if (user.age < 18) {
          return APIResult.Error(HttpStatusCode.BadRequest, "User is underaged")
        }

        userRepository.addUser(user)
        return APIResult.Success(UserRegistrationResult(user, true))
      }
    }
  }

  private fun userFromJwtPrincipal(principal: JWTPrincipal?): APIResult<User> {
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
}

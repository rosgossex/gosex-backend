package gosex.backend.service

import gosex.backend.model.*
import gosex.backend.repository.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

sealed class APIResult<out T> {
  data class Success<out T>(val value: T) : APIResult<T>()

  data class Error(val code: HttpStatus, val message: String) : APIResult<Nothing>()
}

data class UserRegistrationResult(val user: User, val wasCreated: Boolean)

@Service
class UserService(private val userRepository: UserRepository) {

  fun searchUsersByName(nameQuery: String): List<User> {
    return userRepository.findByNameContainingIgnoreCase(nameQuery)
  }

  fun getUserOrRegister(jwt: Jwt?): APIResult<UserRegistrationResult> {
    when (val result = userFromJwt(jwt)) {
      is APIResult.Error -> return result
      is APIResult.Success -> {
        val user = result.value
        val existingUser = userRepository.findById(user.id).orElse(null)
        if (existingUser != null) {
          return APIResult.Success(UserRegistrationResult(existingUser, false))
        }

        if (user.age < 18) {
          return APIResult.Error(HttpStatus.BAD_REQUEST, "User is underaged")
        }

        val savedUser = userRepository.save(user)
        return APIResult.Success(UserRegistrationResult(savedUser, true))
      }
    }
  }

  private fun userFromJwt(jwt: Jwt?): APIResult<User> {
    if (jwt == null) {
      return APIResult.Error(HttpStatus.BAD_REQUEST, "No JWT")
    }

    val userId = jwt.getClaim<String>("sub")
    val givenName = jwt.getClaim<String>("given_name")
    val familyName = jwt.getClaim<String>("family_name")
    val birthdateString = jwt.getClaim<String>("birthdate")
    val genderString = jwt.getClaim<String>("gender")

    if (
      userId == null ||
        givenName == null ||
        familyName == null ||
        birthdateString == null ||
        genderString == null
    ) {
      return APIResult.Error(
        HttpStatus.BAD_REQUEST,
        "Missing required JWT claims: userId=$userId givenName=$givenName familyName=$familyName birthdateString=$birthdateString genderString=$genderString",
      )
    }

    val birthdate =
      try {
        LocalDate.parse(birthdateString)
      } catch (e: DateTimeParseException) {
        return APIResult.Error(HttpStatus.BAD_REQUEST, "Invalid birthdate format in JWT")
      }

    val gender =
      when (genderString) {
        "male" -> Gender.Male
        "female" -> Gender.Female
        else -> return APIResult.Error(HttpStatus.BAD_REQUEST, "Invalid gender in JWT")
      }

    val user =
      User(
        id = userId,
        birthdate = birthdate,
        gender = gender,
        givenName = givenName,
        familyName = familyName,
        fullName = "$givenName $familyName",
      )
    return APIResult.Success(user)
  }
}

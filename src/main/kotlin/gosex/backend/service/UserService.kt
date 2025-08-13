package gosex.backend.service

import gosex.backend.dto.ServiceError
import gosex.backend.model.*
import gosex.backend.repository.*
import gosex.backend.util.Result
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

data class UserRegistrationResult(val user: User, val wasCreated: Boolean)

@Service
class UserService(private val userRepository: UserRepository) {

  fun searchUsersByName(nameQuery: String): List<User> {
    return userRepository.findByNameContainingIgnoreCase(nameQuery)
  }

  fun getUserOrRegister(jwt: Jwt?): Result<UserRegistrationResult, ServiceError> {
    when (val result = userFromJwt(jwt)) {
      is Result.Error -> return result
      is Result.Success -> {
        val user = result.value
        val existingUser = userRepository.findById(user.id).orElse(null)
        if (existingUser != null) {
          return Result.Success(UserRegistrationResult(existingUser, false))
        }

        if (user.age < 18) {
          return Result.Error(ServiceError.USER_UNDERAGED)
        }

        val savedUser = userRepository.save(user)
        return Result.Success(UserRegistrationResult(savedUser, true))
      }
    }
  }

  private fun userFromJwt(jwt: Jwt?): Result<User, ServiceError> {
    if (jwt == null) {
      return Result.Error(ServiceError.MISSING_JWT)
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
      return Result.Error(
        ServiceError.MISSING_JWT_CLAIMS,
        ServiceError.MISSING_JWT_CLAIMS.formatMessage(
          "userId=$userId givenName=$givenName familyName=$familyName birthdateString=$birthdateString genderString=$genderString"
        ),
      )
    }

    val birthdate =
      try {
        LocalDate.parse(birthdateString)
      } catch (e: DateTimeParseException) {
        return Result.Error(ServiceError.INVALID_BIRTHDATE_FORMAT)
      }

    val gender =
      when (genderString) {
        "male" -> Gender.Male
        "female" -> Gender.Female
        else -> return Result.Error(ServiceError.INVALID_GENDER)
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
    return Result.Success(user)
  }
}

package gosex.backend.controller.dto

import gosex.backend.model.Gender
import gosex.backend.model.User
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate as JavaLocalDate

@Schema(description = "User information")
data class UserDto(
  @Schema(description = "User unique identifier", example = "user-123") val id: String,
  @Schema(description = "User's birth date", example = "1990-05-15") val birthdate: JavaLocalDate,
  @Schema(description = "User's gender") val gender: Gender,
  @Schema(description = "User's given name", example = "John") val givenName: String,
  @Schema(description = "User's family name", example = "Doe") val familyName: String,
  @Schema(description = "User's full name", example = "John Doe") val fullName: String,
) {
  companion object {
    fun fromUser(user: User): UserDto {
      return UserDto(
        id = user.id,
        birthdate = user.birthdate,
        gender = user.gender,
        givenName = user.givenName,
        familyName = user.familyName,
        fullName = user.fullName,
      )
    }
  }
}

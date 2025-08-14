package gosex.backend.dto

import gosex.backend.model.Gender
import gosex.backend.model.User
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User information")
data class UserDto(
  @Schema(description = "User unique identifier", example = "user-123") val id: String,
  @Schema(description = "User's age", example = "25") val age: Int,
  @Schema(description = "User's gender") val gender: Gender,
  @Schema(description = "User's given name", example = "John") val givenName: String,
  @Schema(description = "User's family name", example = "Doe") val familyName: String,
  @Schema(description = "User's full name", example = "John Doe") val fullName: String,
) {
  companion object {
    fun fromUser(user: User): UserDto {
      return UserDto(
        id = user.id,
        age = user.age,
        gender = user.gender,
        givenName = user.givenName,
        familyName = user.familyName,
        fullName = user.fullName,
      )
    }
  }
}

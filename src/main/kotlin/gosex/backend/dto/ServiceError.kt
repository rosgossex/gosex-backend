package gosex.backend.dto

import org.springframework.http.HttpStatus

enum class ServiceError(val httpStatus: HttpStatus, val code: String, val messageTemplate: String) {
  MISSING_QUERY_PARAMETER(
    HttpStatus.BAD_REQUEST,
    "MISSING_QUERY_PARAMETER",
    "Missing or empty 'q' query parameter",
  ),
  USER_UNDERAGED(HttpStatus.BAD_REQUEST, "USER_UNDERAGED", "User is underaged"),
  MISSING_JWT(HttpStatus.BAD_REQUEST, "MISSING_JWT", "No JWT"),
  MISSING_JWT_CLAIMS(
    HttpStatus.BAD_REQUEST,
    "MISSING_JWT_CLAIMS",
    "Missing required JWT claims: %s",
  ),
  INVALID_BIRTHDATE_FORMAT(
    HttpStatus.BAD_REQUEST,
    "INVALID_BIRTHDATE_FORMAT",
    "Invalid birthdate format in JWT",
  ),
  INVALID_GENDER(HttpStatus.BAD_REQUEST, "INVALID_GENDER", "Invalid gender in JWT");

  fun formatMessage(vararg args: Any?): String {
    return if (args.isNotEmpty()) {
      messageTemplate.format(*args)
    } else {
      messageTemplate
    }
  }
}

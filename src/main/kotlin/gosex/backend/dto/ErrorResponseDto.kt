package gosex.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response")
data class ErrorResponseDto(
  @Schema(description = "Error code", example = "MISSING_QUERY_PARAMETER") val code: String,
  @Schema(description = "Error message", example = "Missing or empty 'q' query parameter")
  val message: String,
) {
  companion object {
    fun fromServiceError(serviceError: ServiceError, message: String? = null): ErrorResponseDto {
      return ErrorResponseDto(
        code = serviceError.code,
        message = message ?: serviceError.messageTemplate,
      )
    }
  }
}

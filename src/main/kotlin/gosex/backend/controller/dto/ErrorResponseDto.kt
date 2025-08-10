package gosex.backend.controller.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response")
data class ErrorResponseDto(
  @Schema(description = "Error message", example = "Missing or empty 'q' query parameter")
  val message: String
)

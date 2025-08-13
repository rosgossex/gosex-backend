package gosex.backend.controller

import gosex.backend.dto.ErrorResponseDto
import gosex.backend.dto.ServiceError
import gosex.backend.dto.UserDto
import gosex.backend.service.UserService
import gosex.backend.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management operations")
class UserController(private val userService: UserService) {

  @Operation(
    summary = "Search users by name",
    description = "Search for users by their given name, family name, or full name",
  )
  @ApiResponses(
    value =
      [
        ApiResponse(
          responseCode = "200",
          description = "Users found",
          content =
            [
              Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = UserDto::class)),
              )
            ],
        ),
        ApiResponse(
          responseCode = "400",
          description = "Bad request - missing or empty query parameter",
          content =
            [
              Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponseDto::class),
              )
            ],
        ),
      ]
  )
  @GetMapping("/search")
  fun searchUsers(
    @Parameter(description = "Search query for user name", required = true, example = "John")
    @RequestParam
    q: String
  ): ResponseEntity<Any> {
    if (q.isBlank()) {
      return ResponseEntity.badRequest()
        .body(ErrorResponseDto.fromServiceError(ServiceError.MISSING_QUERY_PARAMETER))
    }
    val users = userService.searchUsersByName(q)
    val userDtos = users.map { UserDto.fromUser(it) }
    return ResponseEntity.ok(userDtos)
  }

  @Operation(
    summary = "Get or register current user",
    description = "Retrieve current user information or register a new user based on JWT claims",
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @ApiResponses(
    value =
      [
        ApiResponse(
          responseCode = "200",
          description = "Existing user retrieved",
          content =
            [
              Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = UserDto::class),
              )
            ],
        ),
        ApiResponse(
          responseCode = "201",
          description = "New user created",
          content =
            [
              Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = UserDto::class),
              )
            ],
        ),
        ApiResponse(
          responseCode = "400",
          description = "Bad request - invalid JWT claims or user is underaged",
          content =
            [
              Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponseDto::class),
              )
            ],
        ),
        ApiResponse(
          responseCode = "401",
          description = "Unauthorized - missing or invalid JWT token",
          content =
            [
              Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponseDto::class),
              )
            ],
        ),
      ]
  )
  @PostMapping("/me")
  fun getUserOrRegister(
    @Parameter(hidden = true) @AuthenticationPrincipal jwt: Jwt
  ): ResponseEntity<Any> {
    return when (val result = userService.getUserOrRegister(jwt)) {
      is Result.Error -> {
        ResponseEntity.status(result.error.httpStatus)
          .body(ErrorResponseDto.fromServiceError(result.error, result.message))
      }
      is Result.Success -> {
        val registrationResult = result.value
        val statusCode =
          if (registrationResult.wasCreated) {
            HttpStatus.CREATED
          } else {
            HttpStatus.OK
          }
        ResponseEntity.status(statusCode).body(UserDto.fromUser(registrationResult.user))
      }
    }
  }
}

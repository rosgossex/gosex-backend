package gosex.backend.controller

import gosex.backend.service.APIResult
import gosex.backend.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

  @GetMapping("/search")
  fun searchUsers(@RequestParam q: String): ResponseEntity<*> {
    if (q.isBlank()) {
      return ResponseEntity.badRequest().body("Missing or empty 'q' query parameter")
    }
    val users = userService.searchUsersByName(q)
    return ResponseEntity.ok(users)
  }

  @PostMapping("/me")
  fun getUserOrRegister(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<*> {
    return when (val result = userService.getUserOrRegister(jwt)) {
      is APIResult.Error -> {
        ResponseEntity.status(result.code).body(result.message)
      }
      is APIResult.Success -> {
        val registrationResult = result.value
        val statusCode =
          if (registrationResult.wasCreated) {
            HttpStatus.CREATED
          } else {
            HttpStatus.OK
          }
        ResponseEntity.status(statusCode).body(registrationResult.user)
      }
    }
  }
}

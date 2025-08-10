package gosex.backend.service

import gosex.backend.model.Gender
import gosex.backend.model.User
import gosex.backend.repository.UserRepository
import java.time.Instant
import java.time.LocalDate
import java.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt

class UserServiceTest {

  private lateinit var mockUserRepository: UserRepository
  private lateinit var userService: UserService

  @BeforeEach
  fun setUp() {
    mockUserRepository = mock<UserRepository>()
    userService = UserService(mockUserRepository)
  }

  private fun createJwt(
    sub: String? = "user123",
    givenName: String? = "John",
    familyName: String? = "Doe",
    birthdate: String? = "1990-01-01",
    gender: String? = "male",
  ): Jwt {
    val claims = mutableMapOf<String, Any>()
    sub?.let { claims["sub"] = it }
    givenName?.let { claims["given_name"] = it }
    familyName?.let { claims["family_name"] = it }
    birthdate?.let { claims["birthdate"] = it }
    gender?.let { claims["gender"] = it }

    return Jwt.withTokenValue("token")
      .header("alg", "RS256")
      .claims { it.putAll(claims) }
      .issuedAt(Instant.now())
      .expiresAt(Instant.now().plusSeconds(3600))
      .build()
  }

  @Test
  fun `searchUsersByName should delegate to repository`() {
    val nameQuery = "John"
    val expectedUsers =
      listOf(
        User(
          id = "user1",
          birthdate = LocalDate.of(1990, 1, 1),
          gender = Gender.Male,
          givenName = "John",
          familyName = "Doe",
          fullName = "John Doe",
        )
      )

    whenever(mockUserRepository.findByNameContainingIgnoreCase(nameQuery)).thenReturn(expectedUsers)

    val result = userService.searchUsersByName(nameQuery)

    assertEquals(expectedUsers, result)
    verify(mockUserRepository).findByNameContainingIgnoreCase(nameQuery)
  }

  @Test
  fun `getUserOrRegister should return error when JWT is null`() {
    val result = userService.getUserOrRegister(null)

    assertTrue(result is APIResult.Error)
    result as APIResult.Error
    assertEquals(HttpStatus.BAD_REQUEST, result.code)
    assertEquals("No JWT", result.message)
  }

  @Test
  fun `getUserOrRegister should return error when JWT claims are missing`() {
    val jwt =
      createJwt(sub = null, givenName = null, familyName = null, birthdate = null, gender = null)

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Error)
    result as APIResult.Error
    assertEquals(HttpStatus.BAD_REQUEST, result.code)
    assertTrue(result.message.contains("Missing required JWT claims"))
  }

  @Test
  fun `getUserOrRegister should return error when birthdate format is invalid`() {
    val jwt = createJwt(birthdate = "invalid-date")

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Error)
    result as APIResult.Error
    assertEquals(HttpStatus.BAD_REQUEST, result.code)
    assertEquals("Invalid birthdate format in JWT", result.message)
  }

  @Test
  fun `getUserOrRegister should return error when gender is invalid`() {
    val jwt = createJwt(gender = "invalid")

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Error)
    result as APIResult.Error
    assertEquals(HttpStatus.BAD_REQUEST, result.code)
    assertEquals("Invalid gender in JWT", result.message)
  }

  @Test
  fun `getUserOrRegister should return error when user is underaged`() {
    val today = LocalDate.now()
    val underagedBirthdate = today.minusYears(17)
    val jwt = createJwt(birthdate = underagedBirthdate.toString())

    whenever(mockUserRepository.findById("user123")).thenReturn(Optional.empty())

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Error)
    result as APIResult.Error
    assertEquals(HttpStatus.BAD_REQUEST, result.code)
    assertEquals("User is underaged", result.message)
    verify(mockUserRepository, never()).save(any())
  }

  @Test
  fun `getUserOrRegister should return existing user when found`() {
    val existingUser =
      User(
        id = "user123",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )

    val jwt = createJwt()

    whenever(mockUserRepository.findById("user123")).thenReturn(Optional.of(existingUser))

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Success<UserRegistrationResult>)
    result as APIResult.Success<UserRegistrationResult>
    assertEquals(existingUser, result.value.user)
    assertFalse(result.value.wasCreated)
    verify(mockUserRepository, never()).save(any())
  }

  @Test
  fun `getUserOrRegister should create new user when not found and user is of age`() {
    val jwt = createJwt()
    val expectedUser =
      User(
        id = "user123",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )

    whenever(mockUserRepository.findById("user123")).thenReturn(Optional.empty())
    whenever(mockUserRepository.save(any<User>())).thenReturn(expectedUser)

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Success<UserRegistrationResult>)
    result as APIResult.Success<UserRegistrationResult>
    assertEquals("user123", result.value.user.id)
    assertEquals("John", result.value.user.givenName)
    assertEquals("Doe", result.value.user.familyName)
    assertEquals(LocalDate.of(1990, 1, 1), result.value.user.birthdate)
    assertEquals(Gender.Male, result.value.user.gender)
    assertTrue(result.value.wasCreated)
    verify(mockUserRepository).save(any<User>())
  }

  @Test
  fun `getUserOrRegister should handle female gender correctly`() {
    val jwt = createJwt(givenName = "Jane", gender = "female")
    val expectedUser =
      User(
        id = "user123",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Female,
        givenName = "Jane",
        familyName = "Doe",
        fullName = "Jane Doe",
      )

    whenever(mockUserRepository.findById("user123")).thenReturn(Optional.empty())
    whenever(mockUserRepository.save(any<User>())).thenReturn(expectedUser)

    val result = userService.getUserOrRegister(jwt)

    assertTrue(result is APIResult.Success<UserRegistrationResult>)
    result as APIResult.Success<UserRegistrationResult>
    assertEquals(Gender.Female, result.value.user.gender)
    assertTrue(result.value.wasCreated)
  }
}

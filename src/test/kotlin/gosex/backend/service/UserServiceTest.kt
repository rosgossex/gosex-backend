package gosex.backend.service

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import gosex.backend.model.Gender
import gosex.backend.model.User
import gosex.backend.repo.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserServiceTest {

  private lateinit var mockUserRepository: UserRepository
  private lateinit var userService: UserService

  @BeforeEach
  fun setUp() {
    mockUserRepository = mock<UserRepository>()
    userService = UserService(mockUserRepository)
  }

  private fun createMockClaim(value: String?): Claim {
    val claim = mock<Claim>()
    whenever(claim.asString()).thenReturn(value)
    return claim
  }

  private fun createJwtPrincipal(
    sub: String? = "user123",
    givenName: String? = "John",
    familyName: String? = "Doe",
    birthdate: String? = "1990-01-01",
    gender: String? = "male",
  ): JWTPrincipal {
    val subClaim = createMockClaim(sub)
    val givenNameClaim = createMockClaim(givenName)
    val familyNameClaim = createMockClaim(familyName)
    val birthdateClaim = createMockClaim(birthdate)
    val genderClaim = createMockClaim(gender)

    val mockPayload = mock<Payload>()
    whenever(mockPayload.getClaim("sub")).thenReturn(subClaim)
    whenever(mockPayload.getClaim("given_name")).thenReturn(givenNameClaim)
    whenever(mockPayload.getClaim("family_name")).thenReturn(familyNameClaim)
    whenever(mockPayload.getClaim("birthdate")).thenReturn(birthdateClaim)
    whenever(mockPayload.getClaim("gender")).thenReturn(genderClaim)

    val mockPrincipal = mock<JWTPrincipal>()
    whenever(mockPrincipal.payload).thenReturn(mockPayload)
    return mockPrincipal
  }

  @Test
  fun `searchUsersByName should delegate to repository`() = runBlocking {
    val nameQuery = "John"
    val expectedUsers =
      listOf(
        User(
          id = "user1",
          birthdate = LocalDate(1990, 1, 1),
          gender = Gender.Male,
          givenName = "John",
          familyName = "Doe",
        )
      )

    whenever(mockUserRepository.usersByName(nameQuery)).thenReturn(expectedUsers)

    val result = userService.searchUsersByName(nameQuery)

    assertEquals(expectedUsers, result)
    verify(mockUserRepository).usersByName(nameQuery)
  }

  @Test
  fun `getUserOrRegister should return error when JWT principal is null`() = runBlocking {
    val result = userService.getUserOrRegister(null)

    assertIs<APIResult.Error>(result)
    assertEquals(HttpStatusCode.BadRequest, result.code)
    assertEquals("No JWT Principal", result.message)
  }

  @Test
  fun `getUserOrRegister should return error when JWT claims are missing`() = runBlocking {
    val mockPrincipal =
      createJwtPrincipal(
        sub = null,
        givenName = null,
        familyName = null,
        birthdate = null,
        gender = null,
      )

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Error>(result)
    assertEquals(HttpStatusCode.BadRequest, result.code)
    assertTrue(result.message.contains("Missing required JWT claims"))
  }

  @Test
  fun `getUserOrRegister should return error when birthdate format is invalid`() = runBlocking {
    val mockPrincipal = createJwtPrincipal(birthdate = "invalid-date")

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Error>(result)
    assertEquals(HttpStatusCode.BadRequest, result.code)
    assertEquals("Invalid birthdate format in JWT", result.message)
  }

  @Test
  fun `getUserOrRegister should return error when gender is invalid`() = runBlocking {
    val mockPrincipal = createJwtPrincipal(gender = "invalid")

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Error>(result)
    assertEquals(HttpStatusCode.BadRequest, result.code)
    assertEquals("Invalid gender in JWT", result.message)
  }

  @Test
  fun `getUserOrRegister should return error when user is underaged`() = runBlocking {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val underagedBirthdate = today.minus(DatePeriod(years = 17))
    val mockPrincipal = createJwtPrincipal(birthdate = underagedBirthdate.toString())

    whenever(mockUserRepository.userById("user123")).thenReturn(null)

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Error>(result)
    assertEquals(HttpStatusCode.BadRequest, result.code)
    assertEquals("User is underaged", result.message)
    verify(mockUserRepository, never()).addUser(any())
  }

  @Test
  fun `getUserOrRegister should return existing user when found`() = runBlocking {
    val existingUser =
      User(
        id = "user123",
        birthdate = LocalDate(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
      )

    val mockPrincipal = createJwtPrincipal()

    whenever(mockUserRepository.userById("user123")).thenReturn(existingUser)

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Success<UserRegistrationResult>>(result)
    assertEquals(existingUser, result.value.user)
    assertFalse(result.value.wasCreated)
    verify(mockUserRepository, never()).addUser(any())
  }

  @Test
  fun `getUserOrRegister should create new user when not found and user is of age`() = runBlocking {
    val mockPrincipal = createJwtPrincipal()

    whenever(mockUserRepository.userById("user123")).thenReturn(null)

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Success<UserRegistrationResult>>(result)
    assertEquals("user123", result.value.user.id)
    assertEquals("John", result.value.user.givenName)
    assertEquals("Doe", result.value.user.familyName)
    assertEquals(LocalDate(1990, 1, 1), result.value.user.birthdate)
    assertEquals(Gender.Male, result.value.user.gender)
    assertTrue(result.value.wasCreated)
    verify(mockUserRepository).addUser(result.value.user)
  }

  @Test
  fun `getUserOrRegister should handle female gender correctly`() = runBlocking {
    val mockPrincipal = createJwtPrincipal(givenName = "Jane", gender = "female")

    whenever(mockUserRepository.userById("user123")).thenReturn(null)

    val result = userService.getUserOrRegister(mockPrincipal)

    assertIs<APIResult.Success<UserRegistrationResult>>(result)
    assertEquals(Gender.Female, result.value.user.gender)
    assertTrue(result.value.wasCreated)
  }
}

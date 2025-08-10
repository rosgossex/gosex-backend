package gosex.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import gosex.backend.model.Gender
import gosex.backend.model.User
import gosex.backend.repository.UserRepository
import java.time.LocalDate
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  properties =
    [
      "gosex.esia.url=http://localhost:8080",
      "spring.datasource.url=jdbc:h2:mem:testdb",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
class UserControllerIntegrationTest {

  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper
  @MockBean private lateinit var userRepository: UserRepository
  @MockBean private lateinit var jwtDecoder: JwtDecoder

  private lateinit var testUser: User

  @BeforeEach
  fun setUp() {
    testUser =
      User(
        id = "user123",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )
  }

  @Test
  fun `GET users search should return users when query matches`() {
    // Given
    val users = listOf(testUser)
    whenever(userRepository.findByNameContainingIgnoreCase("John")).thenReturn(users)

    // When & Then
    mockMvc
      .perform(get("/users/search").param("q", "John").with(jwt()))
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id").value("user123"))
      .andExpect(jsonPath("$[0].givenName").value("John"))
      .andExpect(jsonPath("$[0].familyName").value("Doe"))
  }

  @Test
  fun `GET users search should return bad request when query is empty`() {
    mockMvc
      .perform(get("/users/search").param("q", "").with(jwt()))
      .andExpect(status().isBadRequest)
      .andExpect(content().string("Missing or empty 'q' query parameter"))
  }

  @Test
  fun `POST users me should create new user when not exists`() {
    // Given
    whenever(userRepository.findById("user123")).thenReturn(Optional.empty())
    whenever(userRepository.save(org.mockito.kotlin.any<User>())).thenReturn(testUser)

    // When & Then
    mockMvc
      .perform(
        post("/users/me")
          .with(
            jwt().jwt { jwt ->
              jwt
                .claim("sub", "user123")
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .claim("birthdate", "1990-01-01")
                .claim("gender", "male")
                .claim("email_verified", true)
            }
          )
      )
      .andExpect(status().isCreated)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value("user123"))
      .andExpect(jsonPath("$.givenName").value("John"))
      .andExpect(jsonPath("$.familyName").value("Doe"))
  }

  @Test
  fun `POST users me should return existing user when found`() {
    // Given
    whenever(userRepository.findById("user123")).thenReturn(Optional.of(testUser))

    // When & Then
    mockMvc
      .perform(
        post("/users/me")
          .with(
            jwt().jwt { jwt ->
              jwt
                .claim("sub", "user123")
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .claim("birthdate", "1990-01-01")
                .claim("gender", "male")
                .claim("email_verified", true)
            }
          )
      )
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value("user123"))
      .andExpect(jsonPath("$.givenName").value("John"))
  }

  @Test
  fun `POST users me should return bad request when user is underaged`() {
    // Given - user born last year (17 years old)
    val today = LocalDate.now()
    val underagedBirthdate = today.minusYears(17)

    // When & Then
    mockMvc
      .perform(
        post("/users/me")
          .with(
            jwt().jwt { jwt ->
              jwt
                .claim("sub", "user123")
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .claim("birthdate", underagedBirthdate.toString())
                .claim("gender", "male")
                .claim("email_verified", true)
            }
          )
      )
      .andExpect(status().isBadRequest)
      .andExpect(content().string("User is underaged"))
  }

  @Test
  fun `should require authentication for protected endpoints`() {
    mockMvc.perform(get("/users/search").param("q", "John")).andExpect(status().isUnauthorized)

    mockMvc.perform(post("/users/me")).andExpect(status().isUnauthorized)
  }
}

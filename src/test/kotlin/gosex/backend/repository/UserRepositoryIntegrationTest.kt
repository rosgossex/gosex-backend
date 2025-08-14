package gosex.backend.repository

import gosex.backend.model.Gender
import gosex.backend.model.User
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
  properties =
    [
      "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///testdb",
      "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
      "spring.jpa.hibernate.ddl-auto=create",
      "spring.jpa.properties.hibernate.hbm2ddl.auto=create",
      "spring.test.database.replace=none",
    ]
)
class UserRepositoryIntegrationTest {

  @Autowired private lateinit var entityManager: TestEntityManager
  @Autowired private lateinit var userRepository: UserRepository

  companion object {
    @Container
    @JvmStatic
    private val postgresContainer =
      PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
      }
  }

  @Test
  fun `should find users by name containing ignore case`() {
    // Given
    val user1 =
      User(
        id = "user1",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )
    val user2 =
      User(
        id = "user2",
        birthdate = LocalDate.of(1992, 3, 15),
        gender = Gender.Female,
        givenName = "Jane",
        familyName = "Smith",
        fullName = "Jane Smith",
      )
    val user3 =
      User(
        id = "user3",
        birthdate = LocalDate.of(1985, 7, 20),
        gender = Gender.Male,
        givenName = "Johnny",
        familyName = "Doe-Smith",
        fullName = "Johnny Doe-Smith",
      )

    entityManager.persist(user1)
    entityManager.persist(user2)
    entityManager.persist(user3)
    entityManager.flush()

    // When
    val johnUsers = userRepository.findByNameContainingIgnoreCase("john")
    val doeUsers = userRepository.findByNameContainingIgnoreCase("DOE")

    // Then
    assertEquals(2, johnUsers.size)
    assertTrue(johnUsers.any { it.id == "user1" })
    assertTrue(johnUsers.any { it.id == "user3" })

    assertEquals(2, doeUsers.size)
    assertTrue(doeUsers.any { it.id == "user1" })
    assertTrue(doeUsers.any { it.id == "user3" })
  }

  @Test
  fun `should return empty list when no users match search`() {
    // Given
    val user =
      User(
        id = "user1",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )
    entityManager.persist(user)
    entityManager.flush()

    // When
    val result = userRepository.findByNameContainingIgnoreCase("NonExistent")

    // Then
    assertTrue(result.isEmpty())
  }

  @Test
  fun `should save and find user by id`() {
    // Given
    val user =
      User(
        id = "user123",
        birthdate = LocalDate.of(1990, 5, 15),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )

    // When
    val savedUser = userRepository.save(user)
    val foundUser = userRepository.findById("user123")

    // Then
    assertEquals(user.id, savedUser.id)
    assertTrue(foundUser.isPresent)
    assertEquals("user123", foundUser.get().id)
    assertEquals("John", foundUser.get().givenName)
    assertEquals("Doe", foundUser.get().familyName)
    assertEquals(Gender.Male, foundUser.get().gender)
  }

  @Test
  fun `should find all users`() {
    // Given
    val user1 =
      User(
        id = "user1",
        birthdate = LocalDate.of(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
        fullName = "John Doe",
      )
    val user2 =
      User(
        id = "user2",
        birthdate = LocalDate.of(1992, 3, 15),
        gender = Gender.Female,
        givenName = "Jane",
        familyName = "Smith",
        fullName = "Jane Smith",
      )

    userRepository.save(user1)
    userRepository.save(user2)

    // When
    val users = userRepository.findAll()

    // Then
    assertEquals(2, users.size)
    assertTrue(users.any { it.id == "user1" })
    assertTrue(users.any { it.id == "user2" })
  }
}

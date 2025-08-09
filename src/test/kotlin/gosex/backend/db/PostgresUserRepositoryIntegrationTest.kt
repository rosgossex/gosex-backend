package gosex.backend.db

import gosex.backend.db.dao.UserTable
import gosex.backend.model.Gender
import gosex.backend.model.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgresUserRepositoryIntegrationTest {

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

  private lateinit var database: Database
  private lateinit var repository: PostgresUserRepository

  @BeforeAll
  fun setUp() {
    if (!postgresContainer.isRunning) {
      postgresContainer.start()
    }

    println("Container started, JDBC URL: ${postgresContainer.jdbcUrl}")
    println("Container ready, connecting to database...")

    database =
      Database.connect(
        postgresContainer.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgresContainer.username,
        password = postgresContainer.password,
      )
    TransactionManager.defaultDatabase = database

    transaction(database) { SchemaUtils.create(UserTable) }
    repository = PostgresUserRepository()
  }

  @AfterAll
  fun tearDown() {
    postgresContainer.stop()
  }

  @AfterEach
  fun cleanUp() {
    transaction(database) { UserTable.deleteAll() }
  }

  @Test
  fun `should add user and find by id`() = runBlocking {
    val user =
      User(
        id = "user123",
        birthdate = LocalDate(1990, 5, 15),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
      )

    repository.addUser(user)
    val foundUser = repository.userById("user123")

    assertNotNull(foundUser)
    assertEquals("user123", foundUser.id)
    assertEquals(LocalDate(1990, 5, 15), foundUser.birthdate)
    assertEquals(Gender.Male, foundUser.gender)
    assertEquals("John", foundUser.givenName)
    assertEquals("Doe", foundUser.familyName)
    assertEquals("John Doe", foundUser.fullName)
  }

  @Test
  fun `should return null when user not found`() = runBlocking {
    val foundUser = repository.userById("nonexistent")

    assertNull(foundUser)
  }

  @Test
  fun `should find all users`() = runBlocking {
    val user1 =
      User(
        id = "user1",
        birthdate = LocalDate(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
      )
    val user2 =
      User(
        id = "user2",
        birthdate = LocalDate(1992, 3, 15),
        gender = Gender.Female,
        givenName = "Jane",
        familyName = "Smith",
      )

    repository.addUser(user1)
    repository.addUser(user2)

    val users = repository.allUsers()

    assertEquals(2, users.size)
    assertTrue(users.any { it.id == "user1" })
    assertTrue(users.any { it.id == "user2" })
  }

  @Test
  fun `should find users by name`() = runBlocking {
    val user1 =
      User(
        id = "user1",
        birthdate = LocalDate(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
      )
    val user2 =
      User(
        id = "user2",
        birthdate = LocalDate(1992, 3, 15),
        gender = Gender.Female,
        givenName = "Jane",
        familyName = "Smith",
      )
    val user3 =
      User(
        id = "user3",
        birthdate = LocalDate(1985, 7, 20),
        gender = Gender.Male,
        givenName = "Johnny",
        familyName = "Doe-Smith",
      )

    repository.addUser(user1)
    repository.addUser(user2)
    repository.addUser(user3)

    val johnUsers = repository.usersByName("John")
    val doeUsers = repository.usersByName("Doe")

    assertEquals(2, johnUsers.size)
    assertTrue(johnUsers.any { it.id == "user1" })
    assertTrue(johnUsers.any { it.id == "user3" })

    assertEquals(2, doeUsers.size)
    assertTrue(doeUsers.any { it.id == "user1" })
    assertTrue(doeUsers.any { it.id == "user3" })
  }

  @Test
  fun `should find no users when search query matches none`() = runBlocking {
    val user =
      User(
        id = "user1",
        birthdate = LocalDate(1990, 1, 1),
        gender = Gender.Male,
        givenName = "John",
        familyName = "Doe",
      )
    repository.addUser(user)

    val users = repository.usersByName("NonExistent")

    assertEquals(0, users.size)
  }
}

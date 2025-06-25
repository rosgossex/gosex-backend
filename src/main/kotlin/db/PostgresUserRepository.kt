package gosex.backend.db

import gosex.backend.db.dao.UserDAO
import gosex.backend.db.dao.UserTable
import gosex.backend.db.dao.daoToModel
import gosex.backend.model.User
import gosex.backend.repo.UserRepository
import org.jetbrains.exposed.sql.*

class PostgresUserRepository : UserRepository {
  override suspend fun allUsers(): List<User> = suspendTransaction {
    UserDAO.all().map(::daoToModel)
  }

  override suspend fun userById(id: String): User? = suspendTransaction {
    UserDAO.findById(id)?.let(::daoToModel)
  }

  override suspend fun addUser(user: User) {
    suspendTransaction {
      UserDAO.new(user.id) {
        birthdate = user.birthdate
        gender = user.gender
        givenName = user.givenName
        familyName = user.familyName
      }
    }
  }

  override suspend fun usersByName(name: String): List<User> = suspendTransaction {
    val searchQuery = "%${name.lowercase()}%"
    UserDAO.find { UserTable.fullName.lowerCase() like searchQuery }.limit(100).map(::daoToModel)
  }
}

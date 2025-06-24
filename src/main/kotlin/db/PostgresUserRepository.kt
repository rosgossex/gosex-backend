package gosex.backend.db

import gosex.backend.db.dao.UserDAO
import gosex.backend.db.dao.UserTable
import gosex.backend.db.dao.daoToModel
import gosex.backend.model.User
import gosex.backend.repo.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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

  override suspend fun removeUserById(id: String): Boolean = suspendTransaction {
    val rowsDeleted = UserTable.deleteWhere { UserTable.id eq id }
    rowsDeleted == 1
  }
}

package gosex.backend.repo

import gosex.backend.model.User

interface UserRepository {
  suspend fun allUsers(): List<User>

  suspend fun userById(id: String): User?

  suspend fun addUser(user: User)

  suspend fun usersByName(name: String): List<User>
}

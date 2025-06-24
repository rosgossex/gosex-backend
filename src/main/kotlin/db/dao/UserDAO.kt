package gosex.backend.db.dao

import gosex.backend.model.Gender
import gosex.backend.model.User
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

object UserTable : IdTable<String>("users") {
  override val id = varchar("id", 36).entityId()
  val birthdate = date("birthdate")
  val gender = enumerationByName("gender", 10, Gender::class)
  val givenName = varchar("given_name", 255)
  val familyName = varchar("family_name", 255)

  override val primaryKey = PrimaryKey(id)
}

class UserDAO(id: EntityID<String>) : Entity<String>(id) {
  companion object : EntityClass<String, UserDAO>(UserTable)

  var birthdate by UserTable.birthdate
  var gender by UserTable.gender
  var givenName by UserTable.givenName
  var familyName by UserTable.familyName
}

fun daoToModel(dao: UserDAO): User {
  return User(
    id = dao.id.value,
    birthdate = dao.birthdate,
    gender = dao.gender,
    givenName = dao.givenName,
    familyName = dao.familyName,
  )
}

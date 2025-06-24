package gosex.backend

import gosex.backend.db.dao.*
import io.ktor.server.application.*
import java.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
  val postgresHost = environment.config.property("gosex.backend.postgres.host").getString()
  val postgresPort = environment.config.property("gosex.backend.postgres.port").getString()
  val postgresDatabase = environment.config.property("gosex.backend.postgres.db").getString()
  val postgresUser = environment.config.property("gosex.backend.postgres.user").getString()
  val postgresPassword = environment.config.property("gosex.backend.postgres.password").getString()

  val database =
    Database.connect(
      "jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase",
      user = postgresUser,
      password = postgresPassword,
    )

  transaction(database) { SchemaUtils.create(UserTable) }
}

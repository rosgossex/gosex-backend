package gosex.backend

import gosex.backend.db.PostgresUserRepository
import io.ktor.server.application.*

fun main(args: Array<String>) {
  io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
  val userRepo = PostgresUserRepository()

  configureDatabases()
  configureAuthentication()
  configureSerialization()
  configureRouting(userRepo)
}

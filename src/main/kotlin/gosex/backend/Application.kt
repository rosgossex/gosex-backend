package gosex.backend

import gosex.backend.db.PostgresUserRepository
import gosex.backend.service.UserService
import io.ktor.server.application.*

fun main(args: Array<String>) {
  io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
  val userRepo = PostgresUserRepository()
  val userService = UserService(userRepo)

  configureDatabases()
  configureAuthentication()
  configureSerialization()
  configureResources()
  configureRouting(userService)
}

package gosex.backend

import gosex.backend.model.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
  routing { get("/") { call.respond(listOf(User("Linus", Vibe.Skuf))) } }
}

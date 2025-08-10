package gosex.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class GosExBackendApplication

fun main(args: Array<String>) {
  runApplication<GosExBackendApplication>(*args)
}

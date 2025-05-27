package gosex.backend.model

import kotlinx.serialization.Serializable

enum class Vibe {
  Alt,
  Skuf,
  Normis
}

@Serializable data class User(val name: String, val vibe: Vibe)

package gosex.backend.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

enum class Gender {
  Male,
  Female,
}

@Serializable
data class User(
    val birthdate: LocalDate,
    val gender: Gender,
    val givenName: String,
    val familyName: String,
) {
  val age: Int
    get() {
      val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
      var age = today.year - birthdate.year
      if (today.monthNumber < birthdate.monthNumber ||
          (today.monthNumber == birthdate.monthNumber && today.dayOfMonth < birthdate.dayOfMonth)) {
        age--
      }
      return age
    }
}

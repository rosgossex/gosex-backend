package gosex.backend.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

@Serializable
data class User(
  val id: String,
  val birthdate: LocalDate,
  val gender: Gender,
  val givenName: String,
  val familyName: String,
) {
  val age: Int

  init {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    if (
      today.monthNumber < birthdate.monthNumber ||
        (today.monthNumber == birthdate.monthNumber && today.dayOfMonth < birthdate.dayOfMonth)
    ) {
      age = today.year - birthdate.year - 1
    } else {
      age = today.year - birthdate.year
    }
  }
}

package gosex.backend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.LocalDate

@Entity
@Table(name = "users")
data class User(
  @Id @Column(name = "id", length = 36) val id: String,
  @Column(name = "birthdate", nullable = false) val birthdate: JavaLocalDate,
  @Enumerated(EnumType.STRING) @Column(name = "gender", nullable = false) val gender: Gender,
  @Column(name = "given_name", nullable = false) val givenName: String,
  @Column(name = "family_name", nullable = false) val familyName: String,
  @Column(name = "full_name", nullable = false) val fullName: String = "$givenName $familyName",
) {
  @get:JsonIgnore
  val age: Int
    get() {
      val today = java.time.LocalDate.now()
      return if (
        today.monthValue < birthdate.monthValue ||
          (today.monthValue == birthdate.monthValue && today.dayOfMonth < birthdate.dayOfMonth)
      ) {
        today.year - birthdate.year - 1
      } else {
        today.year - birthdate.year
      }
    }

  constructor() : this("", JavaLocalDate.now(), Gender.Male, "", "", "")
}

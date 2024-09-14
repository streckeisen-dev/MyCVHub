package ch.streckeisen.mycv.backend.applicant

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class Applicant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var firstName: String,
    var lastName: String,
    var email: String,
    var phone: String,
    var birthday: LocalDate
) {
    internal constructor() : this(null, "", "", "", "", LocalDate.now()) {

    }
}
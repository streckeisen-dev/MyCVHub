package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class WorkExperience (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val company: String,
    val positionStart: LocalDate,
    val positionEnd: LocalDate?,
    val location: String,
    val description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val applicant: Applicant
)
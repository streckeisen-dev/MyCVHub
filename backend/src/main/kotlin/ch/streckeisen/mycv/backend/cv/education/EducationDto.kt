package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDate

data class EducationDto (
    val id: Long?,
    val school: String,
    val location: String,
    val educationStart: LocalDate,
    val educationEnd: LocalDate?,
    val degreeName: String,
    val description: String,
    val applicantId: Long
)
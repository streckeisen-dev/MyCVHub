package ch.streckeisen.mycv.backend.cv.education

import java.time.LocalDate

data class EducationDto(
    val id: Long,
    val institution: String,
    val location: String,
    val educationStart: LocalDate,
    val educationEnd: LocalDate?,
    val degreeName: String,
    val description: String
)

package ch.streckeisen.mycv.backend.cv.experience

import java.time.LocalDate

data class WorkExperienceDto(
    val id: Long,
    val jobTitle: String,
    val company: String,
    val positionStart: LocalDate,
    val positionEnd: LocalDate?,
    val location: String,
    val description: String
)

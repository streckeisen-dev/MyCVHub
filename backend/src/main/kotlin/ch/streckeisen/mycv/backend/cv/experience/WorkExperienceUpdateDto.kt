package ch.streckeisen.mycv.backend.cv.experience

import java.time.LocalDate

data class WorkExperienceUpdateDto(
    val id: Long?,
    val jobTitle: String?,
    val location: String?,
    val company: String?,
    val positionStart: LocalDate?,
    val positionEnd: LocalDate?,
    val description: String?
)

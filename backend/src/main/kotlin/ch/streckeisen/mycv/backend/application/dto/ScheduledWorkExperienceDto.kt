package ch.streckeisen.mycv.backend.application.dto

import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceUpdateDto
import java.time.LocalDate

data class ScheduledWorkExperienceDto(
    val jobTitle: String?,
    val location: String?,
    val company: String?,
    val positionStart: LocalDate?,
    val description: String?
) {
    fun toUpdateRequest() = WorkExperienceUpdateDto(
        id = null,
        jobTitle = jobTitle,
        location = location,
        company = company,
        positionStart = positionStart,
        positionEnd = null,
        description = description
    )
}

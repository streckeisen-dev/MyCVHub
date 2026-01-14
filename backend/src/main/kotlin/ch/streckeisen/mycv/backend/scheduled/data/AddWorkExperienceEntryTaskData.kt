package ch.streckeisen.mycv.backend.scheduled.data

import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceUpdateDto
import java.io.Serializable
import java.time.LocalDate

data class AddWorkExperienceEntryTaskData(
    val accountId: Long,
    val jobTitle: String,
    val company: String,
    val location: String,
    val startDate: LocalDate,
    val description: String
) : Serializable {
    fun toUpdateRequest(): WorkExperienceUpdateDto = WorkExperienceUpdateDto(
        id = null,
        jobTitle = jobTitle,
        company = company,
        location = location,
        positionStart = startDate,
        positionEnd = null,
        description = description
    )
}
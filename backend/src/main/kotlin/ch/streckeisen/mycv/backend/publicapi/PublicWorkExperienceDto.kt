package ch.streckeisen.mycv.backend.publicapi

import java.time.LocalDate

data class PublicWorkExperienceDto(
    val id: Long?,
    val company: String,
    val positionStart: LocalDate,
    val positionEnd: LocalDate?,
    val location: String,
    val description: String
)
package ch.streckeisen.mycv.backend.publicapi.profile.dto

import java.time.LocalDate

data class PublicEducationDto (
    val id: Long?,
    val school: String,
    val location: String,
    val educationStart: LocalDate,
    val educationEnd: LocalDate?,
    val degreeName: String,
    val description: String
)
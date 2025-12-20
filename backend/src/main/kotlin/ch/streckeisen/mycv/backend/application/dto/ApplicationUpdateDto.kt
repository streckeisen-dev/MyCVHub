package ch.streckeisen.mycv.backend.application.dto

import ch.streckeisen.mycv.backend.application.ApplicationStatus

data class ApplicationUpdateDto(
    val id: Long?,
    val jobTitle: String?,
    val company: String?,
    val status: ApplicationStatus?,
    val source: String?,
    val description: String?,
)

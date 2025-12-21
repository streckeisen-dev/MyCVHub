package ch.streckeisen.mycv.backend.application.dto

data class ApplicationUpdateDto(
    val id: Long?,
    val jobTitle: String?,
    val company: String?,
    val source: String?,
    val description: String?,
)

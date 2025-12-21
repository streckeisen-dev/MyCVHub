package ch.streckeisen.mycv.backend.application.dto

data class ApplicationTransitionRequestDto(
    val applicationId: Long?,
    val comment: String?
)
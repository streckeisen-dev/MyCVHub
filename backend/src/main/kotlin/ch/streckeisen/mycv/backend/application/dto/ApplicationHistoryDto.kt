package ch.streckeisen.mycv.backend.application.dto

import java.time.LocalDateTime

data class ApplicationHistoryDto(
    val id: Long,
    val from: ApplicationStatusDto,
    val to: ApplicationStatusDto,
    val comment: String?,
    val timestamp: LocalDateTime
)
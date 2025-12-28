package ch.streckeisen.mycv.backend.application.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ApplicationDetailsDto(
    val id: Long,
    val jobTitle: String,
    val company: String,
    val status: ApplicationStatusDto,
    val source: String?,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val history: List<ApplicationHistoryDto>,
    val transitions: List<ApplicationTransitionDto>,
    @field:JsonProperty(value = "isArchived")
    val isArchived: Boolean
)
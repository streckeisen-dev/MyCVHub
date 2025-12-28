package ch.streckeisen.mycv.backend.application.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ApplicationSearchDto(
    val id: Long,
    val jobTitle: String,
    val company: String,
    val status: ApplicationStatusDto,
    val source: String?,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    @field:JsonProperty(value = "isArchived")
    val isArchived: Boolean
)


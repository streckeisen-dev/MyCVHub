package ch.streckeisen.mycv.backend.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ApplicationStatusDto(
    val key: String,
    val name: String,
    @field:JsonProperty(value = "isTerminal")
    val isTerminal: Boolean
)

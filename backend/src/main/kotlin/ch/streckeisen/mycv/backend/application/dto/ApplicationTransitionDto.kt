package ch.streckeisen.mycv.backend.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ApplicationTransitionDto(
    val id: Int,
    val label: String,
    @field:JsonProperty(value = "isHired")
    val isHired: Boolean
)

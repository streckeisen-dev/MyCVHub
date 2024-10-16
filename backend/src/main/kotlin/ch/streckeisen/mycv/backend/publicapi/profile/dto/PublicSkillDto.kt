package ch.streckeisen.mycv.backend.publicapi.profile.dto

data class PublicSkillDto(
    val name: String,
    val type: String,
    val level: Short
)
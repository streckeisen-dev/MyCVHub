package ch.streckeisen.mycv.backend.applicationTemplate.dto

data class CoverLetterConfigurationDto(
    val style: String,
    val language: String,
    val mirrorProfileImage: Boolean,
    val content: String,
    val closing: String,
    val documents: List<String>?
)
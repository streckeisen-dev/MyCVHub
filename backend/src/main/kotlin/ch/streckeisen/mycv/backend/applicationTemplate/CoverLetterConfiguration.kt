package ch.streckeisen.mycv.backend.applicationTemplate

data class CoverLetterConfiguration(
    val style: String,
    val language: String,
    val mirrorProfileImage: Boolean,
    val content: String,
    val closing: String,
    val documents: List<String>?
)

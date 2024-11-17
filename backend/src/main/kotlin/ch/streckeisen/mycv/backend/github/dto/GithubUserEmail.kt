package ch.streckeisen.mycv.backend.github.dto

data class GithubUserEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)

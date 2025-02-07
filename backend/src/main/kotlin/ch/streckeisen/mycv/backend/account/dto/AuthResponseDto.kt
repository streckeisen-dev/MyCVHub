package ch.streckeisen.mycv.backend.account.dto

data class AuthResponseDto(
    val token: String,
    val expiresIn: Long,
)

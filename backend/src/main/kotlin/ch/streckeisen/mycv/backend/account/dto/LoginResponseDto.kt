package ch.streckeisen.mycv.backend.account.dto

data class LoginResponseDto(
    val token: String,
    val expiresIn: Long,
)

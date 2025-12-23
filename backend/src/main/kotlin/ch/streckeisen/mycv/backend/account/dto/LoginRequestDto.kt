package ch.streckeisen.mycv.backend.account.dto

data class LoginRequestDto(
    val username: String?,
    val password: String?
)
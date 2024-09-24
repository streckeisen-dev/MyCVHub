package ch.streckeisen.mycv.backend.account

data class LoginResponseDto(
    val token: String,
    val expiresIn: Long,
)

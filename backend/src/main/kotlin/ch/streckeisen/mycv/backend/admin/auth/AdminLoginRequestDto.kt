package ch.streckeisen.mycv.backend.admin.auth

data class AdminLoginRequestDto(
    val username: String?,
    val password: String?
)

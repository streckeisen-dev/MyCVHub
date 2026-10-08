package ch.streckeisen.mycv.backend.admin.auth

data class AdminChangePasswordDto(
    val password: String?,
    val confirmPassword: String?
)

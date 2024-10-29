package ch.streckeisen.mycv.backend.account.dto

data class ChangePasswordDto(
    val oldPassword: String?,
    val password: String?,
    val confirmPassword: String?
)

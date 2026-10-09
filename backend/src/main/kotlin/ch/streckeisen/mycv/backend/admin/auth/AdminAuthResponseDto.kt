package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.admin.account.AdminRole

data class AdminAuthResponseDto(
    val username: String,
    val role: AdminRole,
    val mustChangePassword: Boolean
)

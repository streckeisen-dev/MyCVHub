package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.admin.account.AdminRole

data class AdminPrincipal(
    val username: String,
    val id: Long,
    val role: AdminRole,
    val mustChangePassword: Boolean
)

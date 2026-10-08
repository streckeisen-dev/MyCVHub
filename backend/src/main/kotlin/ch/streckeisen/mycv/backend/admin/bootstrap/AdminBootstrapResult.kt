package ch.streckeisen.mycv.backend.admin.bootstrap

import java.time.LocalDateTime

data class AdminBootstrapResult(
    val username: String,
    val temporaryPassword: String,
    val expiresAt: LocalDateTime
)

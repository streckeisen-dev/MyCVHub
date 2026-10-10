package ch.streckeisen.mycv.backend.admin.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "my-cv.admin.auth.rate-limit")
data class AdminLoginRateLimitProperties(
    val maxFailedAttempts: Int = 5,
    val lockoutMinutes: Long = 15
)

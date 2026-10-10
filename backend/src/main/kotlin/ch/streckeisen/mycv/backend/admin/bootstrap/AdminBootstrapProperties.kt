package ch.streckeisen.mycv.backend.admin.bootstrap

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "my-cv.admin.bootstrap")
data class AdminBootstrapProperties(
    val seedUsername: String? = null,
    val username: String? = null,
    val resetExisting: Boolean = false,
    val temporaryPasswordExpirationMinutes: Long = 30
)

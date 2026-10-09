package ch.streckeisen.mycv.backend.admin.auth

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class AdminLoginAttemptEntity(
    @Id
    var username: String,
    var failedAttempts: Int = 0,
    var lockedUntil: LocalDateTime? = null,
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

package ch.streckeisen.mycv.backend.admin.auth

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class AdminLoginRateLimiter(
    private val properties: AdminLoginRateLimitProperties,
    private val adminLoginAttemptRepository: AdminLoginAttemptRepository
) {
    @Transactional
    fun isLocked(username: String): Boolean {
        val attempt = adminLoginAttemptRepository.findByUsernameForUpdate(username).orElse(null) ?: return false
        val lockedUntil = attempt.lockedUntil ?: return false
        if (lockedUntil.isAfter(LocalDateTime.now())) {
            return true
        }
        adminLoginAttemptRepository.delete(attempt)
        return false
    }

    @Transactional
    fun recordFailure(username: String) {
        val attempt = adminLoginAttemptRepository.findByUsernameForUpdate(username).orElse(
            AdminLoginAttemptEntity(username = username)
        )
        attempt.failedAttempts += 1
        attempt.lockedUntil = if (attempt.failedAttempts >= properties.maxFailedAttempts) {
            LocalDateTime.now().plusMinutes(properties.lockoutMinutes)
        } else {
            attempt.lockedUntil
        }
        attempt.updatedAt = LocalDateTime.now()
        adminLoginAttemptRepository.save(attempt)
    }

    @Transactional
    fun recordSuccess(username: String) {
        adminLoginAttemptRepository.findByUsernameForUpdate(username)
            .ifPresent { adminLoginAttemptRepository.delete(it) }
    }
}

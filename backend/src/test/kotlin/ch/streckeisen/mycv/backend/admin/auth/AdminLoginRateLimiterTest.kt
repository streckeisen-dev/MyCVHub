package ch.streckeisen.mycv.backend.admin.auth

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Optional

class AdminLoginRateLimiterTest {
    @Test
    fun testLocksAfterConfiguredFailedAttempts() {
        val repository = inMemoryAttemptRepository()
        val rateLimiter = AdminLoginRateLimiter(
            AdminLoginRateLimitProperties(maxFailedAttempts = 2, lockoutMinutes = 15),
            repository
        )

        rateLimiter.recordFailure("admin@example.com")
        assertFalse(rateLimiter.isLocked("admin@example.com"))

        rateLimiter.recordFailure("admin@example.com")
        assertTrue(rateLimiter.isLocked("admin@example.com"))
    }

    @Test
    fun testSuccessfulLoginClearsFailedAttempts() {
        val repository = inMemoryAttemptRepository()
        val rateLimiter = AdminLoginRateLimiter(
            AdminLoginRateLimitProperties(maxFailedAttempts = 2, lockoutMinutes = 15),
            repository
        )

        rateLimiter.recordFailure("admin@example.com")
        rateLimiter.recordSuccess("admin@example.com")
        rateLimiter.recordFailure("admin@example.com")

        assertFalse(rateLimiter.isLocked("admin@example.com"))
    }

    private fun inMemoryAttemptRepository(): AdminLoginAttemptRepository {
        var storedAttempt: AdminLoginAttemptEntity? = null
        return mockk {
            every { findByUsernameForUpdate(any()) } answers { Optional.ofNullable(storedAttempt) }
            every { save(any()) } answers {
                storedAttempt = firstArg()
                storedAttempt!!
            }
            every { delete(any<AdminLoginAttemptEntity>()) } answers {
                if (storedAttempt?.username == firstArg<AdminLoginAttemptEntity>().username) {
                    storedAttempt = null
                }
            }
        }
    }
}

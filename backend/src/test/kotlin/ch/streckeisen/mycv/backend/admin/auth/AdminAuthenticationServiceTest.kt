package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.account.auth.AuthenticationValidationService
import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

private const val ADMIN_USERNAME = "admin@example.com"
private const val ADMIN_PASSWORD = "Admin-Password-123"
private const val NEW_ADMIN_PASSWORD = "New-Admin-Password-123"
private const val ENCODED_NEW_ADMIN_PASSWORD = "encoded-new-admin-password"

class AdminAuthenticationServiceTest {
    private lateinit var adminAccountRepository: AdminAccountRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var adminAuthTokenService: AdminAuthTokenService
    private lateinit var authenticationValidationService: AuthenticationValidationService
    private lateinit var adminAuditService: AdminAuditService
    private lateinit var adminLoginRateLimiter: AdminLoginRateLimiter
    private lateinit var adminAuthenticationService: AdminAuthenticationService
    private lateinit var admin: AdminAccountEntity

    @BeforeEach
    fun setup() {
        admin = AdminAccountEntity(
            username = ADMIN_USERNAME,
            password = "encoded-admin-password",
            role = AdminRole.SUPER_ADMIN,
            isActive = true,
            mustChangePassword = true,
            temporaryPasswordExpiresAt = LocalDateTime.now().plusMinutes(10),
            id = 1
        )
        adminAccountRepository = mockk {
            every { findByUsername(eq(ADMIN_USERNAME)) } returns Optional.of(admin)
            every { findById(eq(1)) } returns Optional.of(admin)
            every { save(any<AdminAccountEntity>()) } answers { firstArg() }
        }
        passwordEncoder = mockk {
            every { matches(eq(ADMIN_PASSWORD), eq(admin.password)) } returns true
            every { encode(eq(NEW_ADMIN_PASSWORD)) } returns ENCODED_NEW_ADMIN_PASSWORD
        }
        adminAuthTokenService = mockk {
            every { generateAuthData(eq(ADMIN_USERNAME)) } returns Result.success(
                AdminAuthTokens("access", 1000, "refresh", 1000)
            )
        }
        authenticationValidationService = mockk {
            every { validatePassword(eq(NEW_ADMIN_PASSWORD), eq(NEW_ADMIN_PASSWORD), any()) } answers {
                // valid password: leave the provided validation builder untouched
            }
        }
        adminAuditService = mockk(relaxed = true)
        adminLoginRateLimiter = mockk(relaxed = true) {
            every { isLocked(any()) } returns false
        }
        adminAuthenticationService = AdminAuthenticationService(
            adminAccountRepository,
            passwordEncoder,
            adminAuthTokenService,
            authenticationValidationService,
            adminAuditService,
            adminLoginRateLimiter
        )
    }

    @Test
    fun testAuthenticateActiveAdmin() {
        val result = adminAuthenticationService.authenticate(AdminLoginRequestDto(ADMIN_USERNAME, ADMIN_PASSWORD))

        assertTrue(result.isSuccess)
        verify(exactly = 1) { adminAuthTokenService.generateAuthData(eq(ADMIN_USERNAME)) }
        verify(exactly = 1) { adminLoginRateLimiter.recordSuccess(eq(ADMIN_USERNAME)) }
    }

    @Test
    fun testAuthenticateInactiveAdminFails() {
        admin.isActive = false

        val result = adminAuthenticationService.authenticate(AdminLoginRequestDto(ADMIN_USERNAME, ADMIN_PASSWORD))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BadCredentialsException)
        verify(exactly = 0) { adminAuthTokenService.generateAuthData(any()) }
        verify(exactly = 1) { adminLoginRateLimiter.recordFailure(eq(ADMIN_USERNAME)) }
    }

    @Test
    fun testAuthenticateLockedAdminFailsBeforePasswordCheck() {
        every { adminLoginRateLimiter.isLocked(eq(ADMIN_USERNAME)) } returns true

        val result = adminAuthenticationService.authenticate(AdminLoginRequestDto(ADMIN_USERNAME, ADMIN_PASSWORD))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BadCredentialsException)
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { adminAuthTokenService.generateAuthData(any()) }
    }

    @Test
    fun testChangePasswordClearsTemporaryPasswordState() {
        val result = adminAuthenticationService.changePassword(
            1,
            AdminChangePasswordDto(NEW_ADMIN_PASSWORD, NEW_ADMIN_PASSWORD)
        )

        assertTrue(result.isSuccess)
        assertFalse(admin.mustChangePassword)
        assertTrue(admin.temporaryPasswordExpiresAt == null)
        assertTrue(admin.passwordChangedAt != null)
        verify(exactly = 1) { adminAuthTokenService.generateAuthData(eq(ADMIN_USERNAME)) }
    }
}

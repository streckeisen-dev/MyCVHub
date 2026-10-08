package ch.streckeisen.mycv.backend.admin.bootstrap

import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

private const val BOOTSTRAP_USERNAME = "admin@example.com"
private const val TEMPORARY_PASSWORD = "Temporary-Password-123"
private const val ENCODED_TEMPORARY_PASSWORD = "encoded-temporary-password"

class AdminBootstrapServiceTest {
    private lateinit var adminAccountRepository: AdminAccountRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var temporaryPasswordGenerator: AdminTemporaryPasswordGenerator
    private lateinit var adminAuditService: AdminAuditService
    private lateinit var adminBootstrapService: AdminBootstrapService
    private lateinit var admin: AdminAccountEntity

    @BeforeEach
    fun setup() {
        admin = AdminAccountEntity(
            username = BOOTSTRAP_USERNAME,
            password = "unusable",
            role = AdminRole.SUPER_ADMIN,
            isActive = false,
            mustChangePassword = true,
            id = 1
        )
        adminAccountRepository = mockk {
            every { findByUsername(eq(BOOTSTRAP_USERNAME)) } returns Optional.of(admin)
            every { save(any<AdminAccountEntity>()) } answers { firstArg() }
        }
        passwordEncoder = mockk {
            every { encode(eq(TEMPORARY_PASSWORD)) } returns ENCODED_TEMPORARY_PASSWORD
        }
        temporaryPasswordGenerator = mockk {
            every { generate() } returns TEMPORARY_PASSWORD
        }
        adminAuditService = mockk(relaxed = true)
        adminBootstrapService = AdminBootstrapService(
            adminAccountRepository,
            passwordEncoder,
            temporaryPasswordGenerator,
            adminAuditService,
            AdminBootstrapProperties(temporaryPasswordExpirationMinutes = 30)
        )
    }

    @Test
    fun testBootstrapInitialSuperAdmin() {
        val result = adminBootstrapService.bootstrap(BOOTSTRAP_USERNAME, false)

        assertEquals(BOOTSTRAP_USERNAME, result.username)
        assertEquals(TEMPORARY_PASSWORD, result.temporaryPassword)
        assertTrue(admin.isActive)
        assertTrue(admin.mustChangePassword)
        assertEquals(ENCODED_TEMPORARY_PASSWORD, admin.password)
        assertTrue(admin.passwordChangedAt != null)
        verify(exactly = 1) { adminAccountRepository.save(eq(admin)) }
    }

    @Test
    fun testRefusesActiveAdminWithoutResetFlag() {
        admin.isActive = true
        admin.mustChangePassword = false

        val result = runCatching { adminBootstrapService.bootstrap(BOOTSTRAP_USERNAME, false) }

        assertTrue(result.isFailure)
        assertFalse(admin.mustChangePassword)
        verify(exactly = 0) { adminAccountRepository.save(any<AdminAccountEntity>()) }
    }
}

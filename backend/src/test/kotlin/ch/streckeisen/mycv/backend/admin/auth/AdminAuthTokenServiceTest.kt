package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.JwtTokenType
import io.jsonwebtoken.JwtException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

private const val TOKEN_USERNAME = "admin@example.com"
private const val REFRESH_TOKEN = "refresh-token"

class AdminAuthTokenServiceTest {
    private lateinit var adminUserDetailsService: AdminUserDetailsService
    private lateinit var jwtService: JwtService
    private lateinit var adminAuthTokenService: AdminAuthTokenService
    private lateinit var admin: AdminAccountEntity

    @BeforeEach
    fun setup() {
        admin = AdminAccountEntity(
            username = TOKEN_USERNAME,
            password = "encoded",
            role = AdminRole.SUPER_ADMIN,
            isActive = true,
            mustChangePassword = true,
            temporaryPasswordExpiresAt = LocalDateTime.now().plusMinutes(10),
            id = 1
        )
        adminUserDetailsService = mockk {
            every { loadAdminByUsernameAsResult(eq(TOKEN_USERNAME)) } returns Result.success(AdminUserDetails(admin))
        }
        jwtService = mockk {
            every { extractUsernameFromRefreshToken(eq(REFRESH_TOKEN)) } returns TOKEN_USERNAME
            every { isRefreshTokenValid(eq(REFRESH_TOKEN), any(), eq(JwtTokenType.ADMIN)) } returns true
        }
        adminAuthTokenService = AdminAuthTokenService(adminUserDetailsService, jwtService)
    }

    @Test
    fun testRefreshTokenRejectedWhenTemporaryPasswordExpired() {
        admin.temporaryPasswordExpiresAt = LocalDateTime.now().minusMinutes(1)

        val result = adminAuthTokenService.validateRefreshToken(REFRESH_TOKEN)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JwtException)
    }

    @Test
    fun testRefreshTokenRejectedWhenIssuedBeforePasswordChange() {
        admin.mustChangePassword = false
        admin.temporaryPasswordExpiresAt = null
        admin.passwordChangedAt = LocalDateTime.now()
        every { jwtService.extractIssuedAtFromRefreshToken(eq(REFRESH_TOKEN)) } returns Date.from(
            admin.passwordChangedAt!!.minusMinutes(1).atZone(ZoneId.systemDefault()).toInstant()
        )

        val result = adminAuthTokenService.validateRefreshToken(REFRESH_TOKEN)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JwtException)
    }
}

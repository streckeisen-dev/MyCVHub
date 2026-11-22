package ch.streckeisen.mycv.backend.security

import io.jsonwebtoken.ExpiredJwtException
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.User
import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.admin.auth.AdminUserDetails

private const val JWT_ACCESS_SECRET = "wHqRTjE8tXtgm/zhvn1qmmxLkcA+MvjnbX0aLkNhLm4="
private const val JWT_REFRESH_SECRET = "AXElBHoFbc5nZ2QACGIsRHY/adM8f7WDRaoI5KsJBGA="
private const val JWT_ACCESS_EXPIRY_TIME = 123456L
private const val JWT_REFRESH_EXPIRY_TIME = 123456789L

class JwtServiceTest {
    private lateinit var jwtService: JwtService

    @Test
    fun adminTokensFollowCurrentCredentialsAndAccountIdentity() {
        val account = AdminAccountEntity(username = "admin", password = "hash-one", role = AdminRole.ADMIN, isActive = true, mustChangePassword = false, id = 1)
        val details = AdminUserDetails(account)
        val access = jwtService.generateAccessToken(details, JwtTokenType.ADMIN)
        val refresh = jwtService.generateRefreshToken(details, JwtTokenType.ADMIN)
        assertTrue(jwtService.isAccessTokenValid(access, details, JwtTokenType.ADMIN))
        assertTrue(jwtService.isRefreshTokenValid(refresh, details, JwtTokenType.ADMIN))
        account.password = "hash-two"
        assertFalse(jwtService.isAccessTokenValid(access, details, JwtTokenType.ADMIN))
        assertFalse(jwtService.isRefreshTokenValid(refresh, details, JwtTokenType.ADMIN))
        val replacement = jwtService.generateAccessToken(details, JwtTokenType.ADMIN)
        assertTrue(jwtService.isAccessTokenValid(replacement, details, JwtTokenType.ADMIN))
        account.id = 2
        assertFalse(jwtService.isAccessTokenValid(replacement, details, JwtTokenType.ADMIN))
    }

    @BeforeEach
    fun setup() {
        jwtService = spyk(JwtService(JWT_ACCESS_SECRET, JWT_REFRESH_SECRET, JWT_ACCESS_EXPIRY_TIME, JWT_REFRESH_EXPIRY_TIME))
    }

    @Test
    fun testGenerateValidAccessToken() {
        val user = User.withUsername("test_user").password("password").build()

        val accessToken = jwtService.generateAccessToken(user)

        assertNotNull(accessToken)
        assertTrue { jwtService.isAccessTokenValid(accessToken, user) }
    }

    @Test
    fun testGenerateValidRefreshToken() {
        val user = User.withUsername("test_user").password("password").build()

        val refreshToken = jwtService.generateRefreshToken(user)

        assertNotNull(refreshToken)
        assertTrue { jwtService.isRefreshTokenValid(refreshToken, user) }
    }

    @Test
    fun testAccessTokenExpirationTime() {
        assertEquals(JWT_ACCESS_EXPIRY_TIME, jwtService.getAccessTokenExpirationTime())
    }

    @Test
    fun testRefreshTokenExpirationTime() {
        assertEquals(JWT_REFRESH_EXPIRY_TIME, jwtService.getRefreshTokenExpirationTime())
    }

    @Test
    fun testIsAccessTokenInValidDueToFalseUsername() {
        val userOne = User.withUsername("test_user_one").password("password").build()
        val userTwo = User.withUsername("test_user_two").password("password").build()
        val token = jwtService.generateAccessToken(userOne)

        val result = jwtService.isAccessTokenValid(token, userTwo)

        assertFalse(result)
    }

    @Test
    fun testIsAccessTokenInvalidDueToExpiration() {
        val user = User.withUsername("test_user").password("password").build()
        val expiredTokenService = JwtService(JWT_ACCESS_SECRET, JWT_REFRESH_SECRET, -1000L, JWT_REFRESH_EXPIRY_TIME)
        val expiredToken = expiredTokenService.generateAccessToken(user)

        assertThrows<ExpiredJwtException> { jwtService.isAccessTokenValid(expiredToken, user) }
    }

    @Test
    fun testIsRefreshTokenInvalidDueToExpiration() {
        val user = User.withUsername("test_user").password("password").build()
        val expiredTokenService = JwtService(JWT_ACCESS_SECRET, JWT_REFRESH_SECRET, JWT_ACCESS_EXPIRY_TIME, -1000L)
        val expiredToken = expiredTokenService.generateRefreshToken(user)

        assertThrows<ExpiredJwtException> { jwtService.isRefreshTokenValid(expiredToken, user) }
    }

    @Test
    fun testExtractUsernameFromAccessToken() {
        val username = "test_user"
        val user = User.withUsername(username).password("password").build()
        val token = jwtService.generateAccessToken(user)

        val extractedUsername = jwtService.extractUsernameFromAccessToken(token)

        assertEquals(username, extractedUsername)
    }

    @Test
    fun testExtractUsernameFromRefreshToken() {
        val username = "test_user"
        val user = User.withUsername(username).password("password").build()
        val token = jwtService.generateRefreshToken(user)

        val extractedUsername = jwtService.extractUsernameFromRefreshToken(token)

        assertEquals(username, extractedUsername)
    }

    @Test
    fun testAccessTokenNotValidWithRefreshKey() {
        val user = User.withUsername("test_user").password("password").build()
        val accessToken = jwtService.generateAccessToken(user)

        assertThrows<Exception> { jwtService.isRefreshTokenValid(accessToken, user) }
    }

    @Test
    fun testRefreshTokenNotValidWithAccessKey() {
        val user = User.withUsername("test_user").password("password").build()
        val refreshToken = jwtService.generateRefreshToken(user)

        assertThrows<Exception> { jwtService.isAccessTokenValid(refreshToken, user) }
    }

    @Test
    fun testApplicantAccessTokenIsNotValidAsAdminToken() {
        val user = User.withUsername("test_user").password("password").build()
        val accessToken = jwtService.generateAccessToken(user, JwtTokenType.APPLICANT)

        assertFalse(jwtService.isAccessTokenValid(accessToken, user, JwtTokenType.ADMIN))
    }

    @Test
    fun testAdminAccessTokenIsNotValidAsApplicantToken() {
        val user = User.withUsername("test_user").password("password").build()
        val accessToken = jwtService.generateAccessToken(user, JwtTokenType.ADMIN)

        assertFalse(jwtService.isAccessTokenValid(accessToken, user, JwtTokenType.APPLICANT))
    }
}

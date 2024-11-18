package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.MyCvUserDetails
import ch.streckeisen.mycv.backend.security.UserDetailsServiceImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val GENERATED_ACCESS_TOKEN = "access_token"
private const val GENERATED_REFRESH_TOKEN = "refresh_token"
private const val ACCESS_TOKEN_EXPIRY_TIME = 123456L
private const val REFRESH_TOKEN_EXPIRY_TIME = 123456789L

class AuthTokenServiceTest {
    private lateinit var userDetailsService: UserDetailsServiceImpl
    private lateinit var jwtService: JwtService

    private lateinit var authTokenService: AuthTokenService

    @BeforeEach
    fun setup() {
        userDetailsService = mockk {
            every { loadUserByUsernameAsResult(any()) } returns Result.failure(BadCredentialsException(""))
            every { loadUserByUsernameAsResult(eq("first.last@example.com")) } returns Result.success(
                MyCvUserDetails(
                    mockk {
                        every { username } returns "first.last@example.com"
                    }
                )
            )
        }

        jwtService = mockk {
            every { generateAccessToken(any()) } returns GENERATED_ACCESS_TOKEN
            every { generateRefreshToken(any()) } returns GENERATED_REFRESH_TOKEN
            every { getAccessTokenExpirationTime() } returns ACCESS_TOKEN_EXPIRY_TIME
            every { getRefreshTokenExpirationTime() } returns REFRESH_TOKEN_EXPIRY_TIME
        }
        authTokenService = AuthTokenService(userDetailsService, jwtService)
    }

    @Test
    fun testGenerateAuthData() {
        val result = authTokenService.generateAuthData("first.last@example.com")

        assertTrue(result.isSuccess)
        val authTokens = result.getOrNull()
        assertNotNull(authTokens)
        assertAuthTokens(authTokens)
    }

    @Test
    fun testGenerateAuthDataWithInvalidUser() {
        val result = authTokenService.generateAuthData("abc")

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is BadCredentialsException)
    }

    @Test
    fun testValidateRefreshToken() {
        val oldRefreshToken = "old_refresh_token"
        val name = "username"
        val user = MyCvUserDetails(
            mockk {
                every { username } returns name
            }
        )

        every { jwtService.extractUsername(eq(oldRefreshToken)) } returns name
        every { userDetailsService.loadUserByUsernameAsResult(eq(name)) } returns Result.success(user)
        every { jwtService.isTokenValid(any(), any()) } returns true

        val validationResult = authTokenService.validateRefreshToken(oldRefreshToken)

        assertTrue { validationResult.isSuccess }
        assertEquals(name, validationResult.getOrNull())
    }

    @Test
    fun testVerifyRefreshTokenWithInvalidToken() {
        val oldRefreshToken = "old_refresh_token"
        every { jwtService.extractUsername(eq(oldRefreshToken)) } returns null
        every { userDetailsService.loadUserByUsernameAsResult(any()) } returns Result.failure(BadCredentialsException(""))

        val refreshResult = authTokenService.validateRefreshToken(oldRefreshToken)

        assertTrue { refreshResult.isFailure }
        assertNotNull(refreshResult.exceptionOrNull())
    }

    @Test
    fun testRefreshAccessTokenWithExpiredToken() {
        val oldRefreshToken = "old_refresh_token"
        val name = "username"
        val user = MyCvUserDetails(
            mockk {
                every { username } returns name
            }
        )

        every { jwtService.extractUsername(eq(oldRefreshToken)) } returns name
        every { userDetailsService.loadUserByUsernameAsResult(eq(name)) } returns Result.success(user)
        every { jwtService.isTokenValid(any(), any()) } returns false

        val refreshResult = authTokenService.validateRefreshToken(oldRefreshToken)

        assertTrue { refreshResult.isFailure }
        assertNotNull(refreshResult.exceptionOrNull())
    }

    private fun assertAuthTokens(authTokens: AuthTokens?) {
        assertNotNull(authTokens)
        assertEquals(GENERATED_REFRESH_TOKEN, authTokens.refreshToken)
        assertEquals(GENERATED_ACCESS_TOKEN, authTokens.accessToken)
        assertEquals(ACCESS_TOKEN_EXPIRY_TIME, authTokens.accessTokenExpirationTime)
        assertEquals(REFRESH_TOKEN_EXPIRY_TIME, authTokens.refreshTokenExpirationTime)
    }
}
package ch.streckeisen.mycv.backend.security

import io.jsonwebtoken.ExpiredJwtException
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.User
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

private const val JWT_SECRET = "AXElBHoFbc5nZ2QACGIsRHY/adM8f7WDRaoI5KsJBGA="
private const val JWT_ACCESS_EXPIRY_TIME = 123456L
private const val JWT_REFRESH_EXPIRY_TIME = 123456789L

class JwtServiceTest {
    private lateinit var jwtService: JwtService

    @BeforeEach
    fun setup() {
        jwtService = spyk(JwtService(JWT_SECRET, JWT_ACCESS_EXPIRY_TIME, JWT_REFRESH_EXPIRY_TIME))
    }

    @Test
    fun testGenerateValidAccessToken() {
        val user = User.withUsername("test_user").password("password").build()

        val accessToken = jwtService.generateAccessToken(user)

        assertNotNull(accessToken)
        assertTrue { jwtService.isTokenValid(accessToken, user) }
    }

    @Test
    fun testGenerateValidRefreshToken() {
        val user = User.withUsername("test_user").password("password").build()

        val refreshToken = jwtService.generateRefreshToken(user)

        assertNotNull(refreshToken)
        assertTrue { jwtService.isTokenValid(refreshToken, user) }
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
    fun testIsTokenInValidDueToFalseUsername() {
        val userOne = User.withUsername("test_user_one").password("password").build()
        val userTwo = User.withUsername("test_user_two").password("password").build()
        val token = jwtService.generateAccessToken(userOne)

        val result = jwtService.isTokenValid(token, userTwo)

        assertFalse(result)
    }

    @Test
    fun testIsTokenInvalidDueToExpiration() {
        val user = User.withUsername("test_user").password("password").build()

        every { jwtService.extractExpiration(any()) } returns Date.from(
            LocalDateTime.now()
                .minusHours(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        )

        val token = jwtService.generateAccessToken(user)

        val result = jwtService.isTokenValid(token, user)

        assertFalse(result)
    }

    @Test
    fun testIsTokenInvalidDueToExpirationByException() {
        val user = User.withUsername("test_user").password("password").build()

        every { jwtService.extractExpiration(any()) } throws mockk<ExpiredJwtException>()

        val token = jwtService.generateAccessToken(user)

        assertThrows<ExpiredJwtException> { jwtService.isTokenValid(token, user) }
    }

    @Test
    fun testExtractUsername() {
        val username = "test_user"
        val user = User.withUsername(username).password("password").build()
        val token = jwtService.generateAccessToken(user)

        val extractedUsername = jwtService.extractUsername(token)

        assertEquals(username, extractedUsername)
    }
}
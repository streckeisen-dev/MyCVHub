package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.security.JwtService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val VALID_SIGNUP_REQUEST = SignupRequestDto(
    "FirstName",
    "LastName",
    "first.last@example.com",
    "+41 79 123 45 67",
    LocalDate.of(2000, 8, 13),
    "StreetName",
    "6",
    "3287",
    "City",
    "CH",
    true,
    "a*c3efgH"
)

private val INVALID_SIGNUP_REQUEST = SignupRequestDto(
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
)

private const val GENERATED_ACCESS_TOKEN = "access_token"
private const val GENERATED_REFRESH_TOKEN = "refresh_token"
private const val ACCESS_TOKEN_EXPIRY_TIME = 123456L
private const val REFRESH_TOKEN_EXPIRY_TIME = 123456789L

class AuthenticationServiceTest {
    private lateinit var applicantAccountService: ApplicantAccountService
    private lateinit var userDetailsService: UserDetailsService
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var jwtService: JwtService
    private lateinit var authenticationService: AuthenticationService

    @BeforeEach
    fun setup() {
        applicantAccountService = mockk {
            every { create(eq(VALID_SIGNUP_REQUEST)) } returns Result.success(
                ApplicantAccountEntity(
                    "New",
                    "Applicant",
                    "new@example.com",
                    "+41 79 987 65 43",
                    LocalDate.of(1987, 1, 6),
                    "Newstreet",
                    null,
                    "123",
                    "NewCity",
                    "CH",
                    "12345678",
                )
            )
            every { create(eq(INVALID_SIGNUP_REQUEST)) } returns Result.failure(mockk<ValidationException>())
        }
        userDetailsService = mockk()
        authenticationManager = mockk()

        jwtService = mockk {
            every { generateAccessToken(any()) } returns GENERATED_ACCESS_TOKEN
            every { generateRefreshToken(any()) } returns GENERATED_REFRESH_TOKEN
            every { getAccessTokenExpirationTime() } returns ACCESS_TOKEN_EXPIRY_TIME
            every { getRefreshTokenExpirationTime() } returns REFRESH_TOKEN_EXPIRY_TIME
        }

        authenticationService = AuthenticationService(
            applicantAccountService,
            userDetailsService,
            authenticationManager,
            jwtService,
        )
    }

    @Test
    fun testSuccessfulSignUp() {
        every { authenticationManager.authenticate(any()) } returns mockk()
        every { userDetailsService.loadUserByUsername(any()) } returns User.withUsername("username")
            .password("pwd")
            .build()

        val signupResult = authenticationService.signUp(VALID_SIGNUP_REQUEST)

        assertTrue { signupResult.isSuccess }
        assertNotNull(signupResult.getOrNull())
    }

    @Test
    fun testFailedSignUp() {
        val signupResult = authenticationService.signUp(INVALID_SIGNUP_REQUEST)

        assertTrue { signupResult.isFailure }
    }

    @Test
    fun testSuccessfulAuthentication() {
        every { authenticationManager.authenticate(any()) } returns mockk()
        every { userDetailsService.loadUserByUsername(eq("existing_username")) } returns User.withUsername("existing_username")
            .password("encoded_password")
            .build()

        val authResult = authenticationService.authenticate(LoginRequestDto("existing_username", "password"))

        assertTrue { authResult.isSuccess }
        val authData = authResult.getOrNull()
        assertNotNull(authData)
        assertAuthData(authData)
    }

    @Test
    fun testAuthenticationWithMissingUsername() {
        val authResult = authenticationService.authenticate(LoginRequestDto(null, "password"))

        assertTrue { authResult.isFailure }
        assertNotNull(authResult.exceptionOrNull())
        assertTrue { authResult.exceptionOrNull() is ValidationException }
    }

    @Test
    fun testAuthenticationWithMissingPassword() {
        val authResult = authenticationService.authenticate(LoginRequestDto("username", null))

        assertTrue { authResult.isFailure }
        assertNotNull(authResult.exceptionOrNull())
        assertTrue { authResult.exceptionOrNull() is ValidationException }
    }

    @Test
    fun testAuthenticationWithAuthenticationException() {
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Authentication failed with bad credentials")

        val authResult = authenticationService.authenticate(LoginRequestDto("username", "password"))

        assertTrue { authResult.isFailure }
        assertNotNull(authResult.exceptionOrNull())
        assertTrue { authResult.exceptionOrNull() is AuthenticationException }
    }

    @Test
    fun testRefreshAccessToken() {
        val oldRefreshToken = "old_refresh_token"
        val username = "username"
        val user = User.withUsername("username")
            .password("pw")
            .build()

        every { jwtService.extractUsername(eq(oldRefreshToken)) } returns username
        every { userDetailsService.loadUserByUsername(eq(username)) } returns user
        every { jwtService.isTokenValid(any(), any()) } returns true

        val refreshResult = authenticationService.refreshAccessToken(oldRefreshToken)

        assertTrue { refreshResult.isSuccess }
        val authData = refreshResult.getOrNull()
        assertAuthData(authData)
    }

    @Test
    fun testRefreshAccessTokenWithInvalidToken() {
        val oldRefreshToken = "old_refresh_token"
        every { jwtService.extractUsername(eq(oldRefreshToken)) } returns null
        every { userDetailsService.loadUserByUsername(any()) } returns null

        val refreshResult = authenticationService.refreshAccessToken(oldRefreshToken)

        assertTrue { refreshResult.isFailure }
        assertNotNull(refreshResult.exceptionOrNull())
    }

    @Test
    fun testRefreshAccessTokenWithExpiredToken() {
        val oldRefreshToken = "old_refresh_token"
        val username = "username"
        val user = User.withUsername("username")
            .password("pw")
            .build()

        every { jwtService.extractUsername(eq(oldRefreshToken)) } returns username
        every { userDetailsService.loadUserByUsername(eq(username)) } returns user
        every { jwtService.isTokenValid(any(), any()) } returns false

        val refreshResult = authenticationService.refreshAccessToken(oldRefreshToken)

        assertTrue { refreshResult.isFailure }
        assertNotNull(refreshResult.exceptionOrNull())
    }

    private fun assertAuthData(authData: AuthData?) {
        assertNotNull(authData)
        assertEquals(GENERATED_REFRESH_TOKEN, authData.refreshToken)
        assertEquals(GENERATED_ACCESS_TOKEN, authData.accessToken)
        assertEquals(ACCESS_TOKEN_EXPIRY_TIME, authData.accessTokenExpirationTime)
        assertEquals(REFRESH_TOKEN_EXPIRY_TIME, authData.refreshTokenExpirationTime)
    }
}
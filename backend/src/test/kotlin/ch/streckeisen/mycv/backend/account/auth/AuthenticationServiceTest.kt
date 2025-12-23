package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationService
import io.jsonwebtoken.JwtException
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.util.Optional

private val existingAccount = ApplicantAccountEntity(
    "username",
    "validPassword",
    false,
    true,
    accountDetails = AccountDetailsEntity(
        "Existing",
        "Applicant",
        "existing@email.com",
        "+41 79 123 45 67",
        LocalDate.of(1985, 6, 25),
        "Realstreet",
        "124a",
        "29742",
        "Real City",
        "CH",
        "de"
    ),
    id = 1,
)

private const val TEST_EMAIL = "first.last@example.com"
private const val VALID_TEST_PASSWORD = "a*c3efgH"

private val validSignupRequest = SignupRequestDto(
    username = TEST_EMAIL,
    firstName = "FirstName",
    lastName = "LastName",
    email = TEST_EMAIL,
    phone = "+41 79 123 45 67",
    birthday = LocalDate.of(2000, 8, 13),
    street = "StreetName",
    houseNumber = "6",
    postcode = "3287",
    city = "City",
    country = "CH",
    password = VALID_TEST_PASSWORD,
    confirmPassword = VALID_TEST_PASSWORD,
    language = "en",
    acceptsTos = true
)

private val invalidSignupRequest = SignupRequestDto(
    username = null,
    firstName = null,
    lastName = null,
    email = null,
    phone = null,
    birthday = null,
    street = null,
    houseNumber = null,
    postcode = null,
    city = null,
    country = null,
    password = null,
    confirmPassword = null,
    language = null,
    acceptsTos = null
)

private val invalidChangePwRequest = ChangePasswordDto(null, null, null)
private val validChangePwRequest = ChangePasswordDto("validPassword", VALID_TEST_PASSWORD, VALID_TEST_PASSWORD)

class AuthenticationServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var authenticationValidationService: AuthenticationValidationService
    private lateinit var authTokenService: AuthTokenService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var accountVerificationService: AccountVerificationService
    private lateinit var authenticationService: AuthenticationService

    private lateinit var accountSaveSlot: CapturingSlot<ApplicantAccountEntity>

    @BeforeEach
    fun setup() {
        accountSaveSlot = slot<ApplicantAccountEntity>()
        applicantAccountRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(existingAccount)
            every { save(capture(accountSaveSlot)) } returns mockk {
                every { id } returns 10
            }
        }

        authenticationManager = mockk()
        authenticationValidationService = mockk {
            every { validateLoginRequest(any()) } returns Result.failure(IllegalArgumentException(""))
            every {
                validateLoginRequest(
                    eq(
                        LoginRequestDto(
                            TEST_EMAIL,
                            VALID_TEST_PASSWORD
                        )
                    )
                )
            } returns Result.success(Unit)
            every { validateSignupRequest(eq(validSignupRequest)) } returns Result.success(Unit)
            every { validateSignupRequest(eq(invalidSignupRequest)) } returns Result.failure(
                IllegalArgumentException(
                    "Invalid signup request"
                )
            )
            every {
                validateChangePasswordRequest(
                    eq(validChangePwRequest),
                    eq(existingAccount.password!!)
                )
            } returns Result.success(Unit)
            every { validateChangePasswordRequest(eq(invalidChangePwRequest), any()) } returns Result.failure(
                IllegalArgumentException("Invalid")
            )
        }

        authTokenService = mockk {
            every { generateAuthData(any()) } returns Result.success(mockk())
            every { generateAuthData(eq("error")) } returns Result.failure(mockk<AuthenticationException>())
        }

        passwordEncoder = mockk {
            every { encode(eq(VALID_TEST_PASSWORD)) } returns "valid_encoded_pw"
        }

        accountVerificationService = mockk()

        authenticationService = AuthenticationService(
            applicantAccountRepository,
            authenticationValidationService,
            authenticationManager,
            authTokenService,
            mockk(relaxed = true),
            passwordEncoder,
            accountVerificationService
        )
    }

    @Test
    fun testSuccessfulSignUp() {
        every { authenticationManager.authenticate(any()) } returns mockk()
        every { accountVerificationService.generateVerificationToken(eq(10)) } returns Result.success(Unit)

        val signupResult = authenticationService.signUp(validSignupRequest)

        assertTrue { signupResult.isSuccess }
        verify(exactly = 1) { applicantAccountRepository.save(any()) }
        verify(exactly = 1) { accountVerificationService.generateVerificationToken(eq(10)) }
        assertNotNull(signupResult.getOrNull())
    }

    @Test
    fun testSuccessfulSignUpWithFailedVerificationTokenGeneration() {
        every { authenticationManager.authenticate(any()) } returns mockk()
        every { accountVerificationService.generateVerificationToken(eq(10)) } returns Result.failure(
            IllegalArgumentException("Failed")
        )

        val signupResult = authenticationService.signUp(validSignupRequest)

        assertTrue { signupResult.isSuccess }
        verify(exactly = 1) { applicantAccountRepository.save(any()) }
        verify(exactly = 1) { accountVerificationService.generateVerificationToken(eq(10)) }
        assertNotNull(signupResult.getOrNull())
    }

    @Test
    fun testFailedSignUp() {
        val signupResult = authenticationService.signUp(invalidSignupRequest)

        assertTrue { signupResult.isFailure }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testSuccessfulChangePassword() {
        every { applicantAccountRepository.setPassword(eq(1), eq("valid_encoded_pw")) } returns Optional.of(
            existingAccount
        )

        val changePasswordResult = authenticationService.changePassword(1, validChangePwRequest)

        assertTrue { changePasswordResult.isSuccess }
        verify(exactly = 1) { applicantAccountRepository.setPassword(any(), any()) }
    }

    @Test
    fun testChangePasswordWithFailingUpdateQuery() {
        every { applicantAccountRepository.setPassword(eq(1), eq("valid_encoded_pw")) } returns Optional.empty()

        val changePasswordResult = authenticationService.changePassword(1, validChangePwRequest)

        assertTrue { changePasswordResult.isFailure }
        verify(exactly = 1) { applicantAccountRepository.setPassword(any(), any()) }
    }

    @Test
    fun testChangePasswordWithMissingAccount() {
        val changePasswordResult = authenticationService.changePassword(2, validChangePwRequest)

        assertTrue { changePasswordResult.isFailure }
        verify(exactly = 0) { authenticationValidationService.validateChangePasswordRequest(any(), any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testSuccessfulAuthentication() {
        every { authenticationManager.authenticate(any()) } returns mockk()

        val authResult = authenticationService.authenticate(LoginRequestDto(TEST_EMAIL, VALID_TEST_PASSWORD))

        assertTrue { authResult.isSuccess }
        assertNotNull(authResult.getOrNull())
    }

    @Test
    fun testFailedAuthenticationWithMissingUsername() {
        val authResult = authenticationService.authenticate(LoginRequestDto(null, null))

        assertTrue { authResult.isFailure }
        assertNotNull(authResult.exceptionOrNull())
        assertTrue { authResult.exceptionOrNull() is IllegalArgumentException }
    }

    @Test
    fun testAuthenticationWithAuthenticationException() {
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Authentication failed with bad credentials")

        val authResult = authenticationService.authenticate(LoginRequestDto(TEST_EMAIL, VALID_TEST_PASSWORD))

        assertTrue { authResult.isFailure }
        assertNotNull(authResult.exceptionOrNull())
        assertTrue { authResult.exceptionOrNull() is AuthenticationException }
    }

    @Test
    fun testRefreshAccessToken() {
        val oldRefreshToken = "old_refresh_token"
        val username = "username"
        every { authTokenService.validateRefreshToken(eq(oldRefreshToken)) } returns Result.success(username)

        val refreshResult = authenticationService.refreshAccessToken(oldRefreshToken)

        assertTrue { refreshResult.isSuccess }
        assertNotNull(refreshResult.getOrNull())
    }

    @Test
    fun testRefreshAccessTokenWithInvalidToken() {
        val oldRefreshToken = "old_refresh_token"
        every { authTokenService.validateRefreshToken(eq(oldRefreshToken)) } returns Result.failure(JwtException("Invalid refresh token"))

        val refreshResult = authenticationService.refreshAccessToken(oldRefreshToken)

        assertTrue { refreshResult.isFailure }
        assertNotNull(refreshResult.exceptionOrNull())
    }
}
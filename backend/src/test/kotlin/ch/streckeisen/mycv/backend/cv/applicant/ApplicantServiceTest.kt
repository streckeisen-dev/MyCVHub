package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.privacy.PrivacySettingsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
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

class ApplicantServiceTest {
    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var applicantValidationService: ApplicantValidationService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var privacySettingsService: PrivacySettingsService
    private lateinit var applicantService: ApplicantService

    @BeforeEach
    fun setup() {
        applicantRepository = mockk {
            every { save(any()) } returns Applicant(
                "New",
                "Applicant",
                null,
                "new@example.com",
                "+41 79 987 65 43",
                LocalDate.of(1987, 1, 6),
                "Newstreet",
                null,
                "123",
                "NewCity",
                "CH",
                "12345678",
                privacySettings = mockk()
            )
        }
        applicantValidationService = mockk {
            every { validateSignupRequest(eq(VALID_SIGNUP_REQUEST)) } returns Result.success(Unit)
            every { validateSignupRequest(eq(INVALID_SIGNUP_REQUEST)) } returns Result.failure(
                IllegalArgumentException(
                    "Invalid signup request"
                )
            )
        }
        passwordEncoder = mockk {
            every { encode(eq("a*c3efgH")) } returns "valid_encoded_pw"
        }
        privacySettingsService = mockk {
            every { getDefaultSettings(any()) } returns mockk()
        }

        applicantService = ApplicantService(applicantRepository, applicantValidationService, passwordEncoder, privacySettingsService)
    }

    @Test
    fun testSuccessfulSignUp() {
        val signupResult = applicantService.create(VALID_SIGNUP_REQUEST)

        assertTrue { signupResult.isSuccess }
        verify(exactly = 1) { applicantRepository.save(any()) }
        assertNotNull(signupResult.getOrNull())
    }

    @Test
    fun testFailedSignUp() {
        val signupResult = applicantService.create(INVALID_SIGNUP_REQUEST)

        assertTrue { signupResult.isFailure }
        verify(exactly = 0) { applicantRepository.save(any()) }
    }
}
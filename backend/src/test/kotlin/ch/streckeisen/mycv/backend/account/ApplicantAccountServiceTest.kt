package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
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
    null
)

class ApplicantAccountServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var applicantAccountService: ApplicantAccountService

    @BeforeEach
    fun setup() {
        applicantAccountRepository = mockk {
            every { save(any()) } returns ApplicantAccountEntity(
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
                "12345678"
            )
        }
        applicantAccountValidationService = mockk {
            every { validateAccountUpdate(eq(VALID_SIGNUP_REQUEST)) } returns Result.success(Unit)
            every { validateAccountUpdate(eq(INVALID_SIGNUP_REQUEST)) } returns Result.failure(
                IllegalArgumentException(
                    "Invalid signup request"
                )
            )
        }
        passwordEncoder = mockk {
            every { encode(eq("a*c3efgH")) } returns "valid_encoded_pw"
        }

        applicantAccountService = ApplicantAccountService(
            applicantAccountRepository,
            applicantAccountValidationService,
            passwordEncoder,
            mockk(relaxed = true)
        )
    }

    @Test
    fun testSuccessfulSignUp() {
        val signupResult = applicantAccountService.create(VALID_SIGNUP_REQUEST)

        assertTrue { signupResult.isSuccess }
        verify(exactly = 1) { applicantAccountRepository.save(any()) }
        assertNotNull(signupResult.getOrNull())
    }

    @Test
    fun testFailedSignUp() {
        val signupResult = applicantAccountService.create(INVALID_SIGNUP_REQUEST)

        assertTrue { signupResult.isFailure }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }
}
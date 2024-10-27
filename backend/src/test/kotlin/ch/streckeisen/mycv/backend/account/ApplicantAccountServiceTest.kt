package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val EXISTING_ACCOUNT = ApplicantAccountEntity(
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
    "validPassword",
    1,
)

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
    "a*c3efgH",
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

private val INVALID_CHANGE_PW_REQUEST = ChangePasswordDto(null, null, null)

private val VALID_CHANGE_PW_REQUEST = ChangePasswordDto("validPassword", "a*c3efgH", "a*c3efgH")

private val INVALID_ACCOUNT_UPDATE = AccountUpdateDto(null, null, null, null, null, null, null, null, null, null)
private val VALID_ACCOUNT_UPDATE = AccountUpdateDto(
    "fN",
    "lN",
    "new2@email.com",
    "+41 78 654 32 20",
    LocalDate.of(2000, 7, 2),
    "st",
    null,
    "pc",
    "ct",
    "DE"
)

class ApplicantAccountServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var applicantAccountService: ApplicantAccountService

    private lateinit var accountSaveSlot: CapturingSlot<ApplicantAccountEntity>

    @BeforeEach
    fun setup() {
        accountSaveSlot = slot<ApplicantAccountEntity>()
        applicantAccountRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(EXISTING_ACCOUNT)
            every { save(capture(accountSaveSlot)) } returns ApplicantAccountEntity(
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
            every { validateSignupRequest(eq(VALID_SIGNUP_REQUEST)) } returns Result.success(Unit)
            every { validateSignupRequest(eq(INVALID_SIGNUP_REQUEST)) } returns Result.failure(
                IllegalArgumentException(
                    "Invalid signup request"
                )
            )
            every {
                validateChangePasswordRequest(
                    eq(VALID_CHANGE_PW_REQUEST),
                    eq(EXISTING_ACCOUNT.password)
                )
            } returns Result.success(Unit)
            every { validateChangePasswordRequest(eq(INVALID_CHANGE_PW_REQUEST), any()) } returns Result.failure(
                IllegalArgumentException("Invalid")
            )
            every { validateAccountUpdate(any(), eq(INVALID_ACCOUNT_UPDATE)) } returns Result.failure(
                IllegalArgumentException("Invalid")
            )
            every { validateAccountUpdate(eq(1), eq(VALID_ACCOUNT_UPDATE)) } returns Result.success(Unit)
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

    @Test
    fun testSuccessfulChangePassword() {
        val changePasswordResult = applicantAccountService.changePassword(1, VALID_CHANGE_PW_REQUEST)

        assertTrue { changePasswordResult.isSuccess }
        verify(exactly = 1) { applicantAccountRepository.save(any()) }

        assertNotNull(accountSaveSlot.captured)
        assertEquals(EXISTING_ACCOUNT.id, accountSaveSlot.captured.id)
        assertEquals(EXISTING_ACCOUNT.firstName, accountSaveSlot.captured.firstName)
        assertEquals(EXISTING_ACCOUNT.lastName, accountSaveSlot.captured.lastName)
        assertEquals(EXISTING_ACCOUNT.email, accountSaveSlot.captured.email)
        assertEquals(EXISTING_ACCOUNT.phone, accountSaveSlot.captured.phone)
        assertEquals(EXISTING_ACCOUNT.birthday, accountSaveSlot.captured.birthday)
        assertEquals(EXISTING_ACCOUNT.street, accountSaveSlot.captured.street)
        assertEquals(EXISTING_ACCOUNT.houseNumber, accountSaveSlot.captured.houseNumber)
        assertEquals(EXISTING_ACCOUNT.postcode, accountSaveSlot.captured.postcode)
        assertEquals(EXISTING_ACCOUNT.city, accountSaveSlot.captured.city)
        assertEquals(EXISTING_ACCOUNT.country, accountSaveSlot.captured.country)
        assertEquals("valid_encoded_pw", accountSaveSlot.captured.password)
    }

    @Test
    fun testChangePasswordWithMissingAccount() {
        val changePasswordResult = applicantAccountService.changePassword(2, VALID_CHANGE_PW_REQUEST)

        assertTrue { changePasswordResult.isFailure }
        verify(exactly = 0) { applicantAccountValidationService.validateChangePasswordRequest(any(), any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testSuccessfulAccountUpdate() {
        val updateResult = applicantAccountService.update(1, VALID_ACCOUNT_UPDATE)
        assertTrue { updateResult.isSuccess }

        verify(exactly = 1) { applicantAccountRepository.save(any()) }

        assertNotNull(accountSaveSlot.captured)
        assertEquals(EXISTING_ACCOUNT.id, accountSaveSlot.captured.id)
        assertEquals(VALID_ACCOUNT_UPDATE.firstName, accountSaveSlot.captured.firstName)
        assertEquals(VALID_ACCOUNT_UPDATE.lastName, accountSaveSlot.captured.lastName)
        assertEquals(VALID_ACCOUNT_UPDATE.email, accountSaveSlot.captured.email)
        assertEquals(VALID_ACCOUNT_UPDATE.phone, accountSaveSlot.captured.phone)
        assertEquals(VALID_ACCOUNT_UPDATE.birthday, accountSaveSlot.captured.birthday)
        assertEquals(VALID_ACCOUNT_UPDATE.street, accountSaveSlot.captured.street)
        assertEquals(VALID_ACCOUNT_UPDATE.houseNumber, accountSaveSlot.captured.houseNumber)
        assertEquals(VALID_ACCOUNT_UPDATE.postcode, accountSaveSlot.captured.postcode)
        assertEquals(VALID_ACCOUNT_UPDATE.city, accountSaveSlot.captured.city)
        assertEquals(VALID_ACCOUNT_UPDATE.country, accountSaveSlot.captured.country)
        assertEquals(EXISTING_ACCOUNT.password, accountSaveSlot.captured.password)
    }

    @Test
    fun testAccountUpdateWithMissingAccount() {
        val updateResult = applicantAccountService.update(2, VALID_ACCOUNT_UPDATE)
        assertTrue { updateResult.isFailure }

        verify(exactly = 0) { applicantAccountValidationService.validateAccountUpdate(any(), any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }
}
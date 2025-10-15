package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationService
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val EXISTING_ACCOUNT = ApplicantAccountEntity(
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

private val INVALID_ACCOUNT_UPDATE = AccountUpdateDto(null, null, null, null, null, null, null, null, null, null, null, null)
private val VALID_ACCOUNT_UPDATE = AccountUpdateDto(
    "username",
    "fN",
    "lN",
    "new2@email.com",
    "+41 78 654 32 20",
    LocalDate.of(2000, 7, 2),
    "st",
    null,
    "pc",
    "ct",
    "DE",
    "de"
)

class ApplicantAccountServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService
    private lateinit var accountVerificationService: AccountVerificationService
    private lateinit var applicantAccountService: ApplicantAccountService

    private lateinit var accountSaveSlot: CapturingSlot<ApplicantAccountEntity>

    @BeforeEach
    fun setup() {
        accountSaveSlot = slot<ApplicantAccountEntity>()
        applicantAccountRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(EXISTING_ACCOUNT)
            every { save(capture(accountSaveSlot)) } returns ApplicantAccountEntity(
                "newusername",
                "12345678",
                false,
                true,
                accountDetails = AccountDetailsEntity(
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
                    "de"
                )
            )
        }
        applicantAccountValidationService = mockk {
            every { validateAccountUpdate(any(), eq(INVALID_ACCOUNT_UPDATE)) } returns Result.failure(
                IllegalArgumentException("Invalid")
            )
            every { validateAccountUpdate(eq(1), eq(VALID_ACCOUNT_UPDATE)) } returns Result.success(Unit)
        }

        accountVerificationService = mockk {
            every { generateVerificationToken(any()) } returns Result.success(Unit)
        }

        applicantAccountService = ApplicantAccountService(
            applicantAccountRepository,
            applicantAccountValidationService,
            accountVerificationService
        )
    }

    @Test
    fun testSuccessfulAccountUpdate() {
        val updateResult = applicantAccountService.update(1, VALID_ACCOUNT_UPDATE)
        assertTrue { updateResult.isSuccess }

        verify(exactly = 1) { applicantAccountRepository.save(any()) }

        assertNotNull(accountSaveSlot.captured)
        assertEquals(EXISTING_ACCOUNT.id, accountSaveSlot.captured.id)
        assertNotNull(accountSaveSlot.captured.accountDetails)
        assertNotNull(accountSaveSlot.captured.accountDetails)
        assertEquals(VALID_ACCOUNT_UPDATE.firstName, accountSaveSlot.captured.accountDetails!!.firstName)
        assertEquals(VALID_ACCOUNT_UPDATE.lastName, accountSaveSlot.captured.accountDetails!!.lastName)
        assertEquals(VALID_ACCOUNT_UPDATE.email, accountSaveSlot.captured.accountDetails!!.email)
        assertEquals(VALID_ACCOUNT_UPDATE.phone, accountSaveSlot.captured.accountDetails!!.phone)
        assertEquals(VALID_ACCOUNT_UPDATE.birthday, accountSaveSlot.captured.accountDetails!!.birthday)
        assertEquals(VALID_ACCOUNT_UPDATE.street, accountSaveSlot.captured.accountDetails!!.street)
        assertEquals(VALID_ACCOUNT_UPDATE.houseNumber, accountSaveSlot.captured.accountDetails!!.houseNumber)
        assertEquals(VALID_ACCOUNT_UPDATE.postcode, accountSaveSlot.captured.accountDetails!!.postcode)
        assertEquals(VALID_ACCOUNT_UPDATE.city, accountSaveSlot.captured.accountDetails!!.city)
        assertEquals(VALID_ACCOUNT_UPDATE.country, accountSaveSlot.captured.accountDetails!!.country)
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
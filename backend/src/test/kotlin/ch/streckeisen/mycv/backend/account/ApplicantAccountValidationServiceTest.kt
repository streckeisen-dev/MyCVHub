package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.util.assertValidationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.Optional
import kotlin.test.BeforeTest

private const val EXISTING_USER_EMAIL = "existing.user@example.com"

class ApplicantAccountValidationServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService

    @BeforeTest
    fun setup() {
        applicantAccountRepository = mockk {
            every { findByUsername(eq(EXISTING_USER_EMAIL)) } returns Optional.of(existingApplicant())
            every { findByUsername(not(eq(EXISTING_USER_EMAIL))) } returns Optional.empty()
        }

        applicantAccountValidationService =
            ApplicantAccountValidationService(applicantAccountRepository, mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("updateAccountValidationDataProvider")
    fun testValidateUpdateAccountRequest(
        accountId: Long,
        accountUpdate: AccountUpdateDto,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        val result = applicantAccountValidationService.validateAccountUpdate(accountId, accountUpdate)
        assertValidationResult(result, isValid, numberOfErrors)
    }

    private fun existingApplicant(): ApplicantAccountEntity = ApplicantAccountEntity(
        "username",
        "abc",
        false,
        true,
        accountDetails = AccountDetailsEntity(
            "Existing",
            "Applicant",
            EXISTING_USER_EMAIL,
            "+41 79 123 45 67",
            LocalDate.of(1985, 6, 25),
            "Realstreet",
            "124a",
            "29742",
            "Real City",
            "CH"
        ),
        id = 1
    )

    companion object {
        @JvmStatic
        fun updateAccountValidationDataProvider() = listOf(
            Arguments.of(
                1,
                AccountUpdateDto(null, null, null, null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                2,
                AccountUpdateDto(
                    "u",
                    "f",
                    "l",
                    EXISTING_USER_EMAIL,
                    "+41 79 345 65 78",
                    LocalDate.of(2010, 4, 5),
                    "s",
                    null,
                    "pc",
                    "c",
                    "CH"
                ),
                false,
                1
            ),
            Arguments.of(
                1,
                AccountUpdateDto(
                    "u",
                    "f",
                    "l",
                    EXISTING_USER_EMAIL,
                    "+41 79 345 65 78",
                    LocalDate.of(2010, 4, 5),
                    "s",
                    null,
                    "pc",
                    "c",
                    "CH"
                ),
                true,
                0
            ),
            Arguments.of(
                2,
                AccountUpdateDto(
                    "u",
                    "f",
                    "l",
                    "another@email.com",
                    "+41 79 345 65 78",
                    LocalDate.of(2010, 4, 5),
                    "s",
                    null,
                    "pc",
                    "c",
                    "CH"
                ),
                true,
                0
            ),
        )
    }
}
package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val EXISTING_USER_EMAIL = "existing.user@example.com"

class ApplicantAccountValidationServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeTest
    fun setup() {
        applicantAccountRepository = mockk {
            every { findByEmail(eq(EXISTING_USER_EMAIL)) } returns Optional.of(existingApplicant())
            every { findByEmail(not(eq(EXISTING_USER_EMAIL))) } returns Optional.empty()
        }
        passwordEncoder = mockk {
            every { matches(any(), any()) } returns false
            every { matches(eq("validPassword"), eq("validEncodedPassword")) } returns true
        }

        applicantAccountValidationService =
            ApplicantAccountValidationService(applicantAccountRepository, mockk(relaxed = true), passwordEncoder)
    }

    @ParameterizedTest
    @MethodSource("signupValidationDataProvider")
    fun testValidateSignupRequest(signupRequest: SignupRequestDto, isValid: Boolean, numberOfErrors: Int) {
        val validationResult = applicantAccountValidationService.validateSignupRequest(signupRequest)
        assertValidationResult(validationResult, isValid, numberOfErrors)
    }

    @ParameterizedTest
    @MethodSource("changePasswordValidationDataProvider")
    fun testValidateChangePasswordRequest(
        changePasswordRequest: ChangePasswordDto,
        currentPassword: String,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        val result =
            applicantAccountValidationService.validateChangePasswordRequest(changePasswordRequest, currentPassword)
        assertValidationResult(result, isValid, numberOfErrors)
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

    private fun assertValidationResult(result: Result<Unit>, isValid: Boolean, numberOfErrors: Int) {
        if (isValid) {
            assertTrue { result.isSuccess }
        } else {
            assertTrue { result.isFailure }
            val throwable = result.exceptionOrNull()
            assertNotNull(throwable)
            assertTrue(throwable is ValidationException)
            assertEquals(numberOfErrors, throwable.errors.size)
        }
    }

    private fun existingApplicant(): ApplicantAccountEntity = ApplicantAccountEntity(
        "Existing",
        "Applicant",
        EXISTING_USER_EMAIL,
        "+41 79 123 45 67",
        LocalDate.of(1985, 6, 25),
        "Realstreet",
        "124a",
        "29742",
        "Real City",
        "CH",
        "abc",
        1,
    )

    companion object {
        @JvmStatic
        fun signupValidationDataProvider() = listOf(
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "FirstNameFirstNameFirstNameFirstNameFirstNameFirstN",
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
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto("FirstName", null, null, null, null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    "LastNameLastNameLastNameLastNameLastNameLastNameLas",
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
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, "LastName", null, null, null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    "firstfirstfirstfirstfirstfirstfirstfirstfirst.lastlastlastlastlastlastlastlastlastlastlast@example.com",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    "first.lastatexample.com",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, "f.l@e.c", null, null, null, null, null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, EXISTING_USER_EMAIL, null, null, null, null, null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    "first.last@example.com",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, "abcd", null, null, null, null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, "123456", null, null, null, null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, "+41 79 123 45 67", null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    null,
                    null,
                    LocalDate.now().plusDays(2),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    null,
                    null,
                    LocalDate.of(1980, 5, 21),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    "LongStreetLongStreetLongStreetLongStreetLongStreetLongStreetLongStreetLongStreetLongStreetLongStreets",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, "StreetName", null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, "12345678901", null, null, null, null, null),
                false,
                12
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, "226a", null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, "1234567890123456", null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, "8000", null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "BigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCityBigCity",
                    null,
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, "City", null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, "GER", null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, "XY", null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, "CH", null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, "abc", null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, "a*c3efgH"),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, "a*c3efgH", "a*c3efgH"),
                false,
                9
            ),
            Arguments.of(
                SignupRequestDto(
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
                ),
                true,
                0
            )
        )

        @JvmStatic
        fun changePasswordValidationDataProvider() = listOf(
            Arguments.of(
                ChangePasswordDto(null, null, null),
                "",
                false,
                3
            ),
            Arguments.of(
                ChangePasswordDto("x", null, null),
                "y",
                false,
                3
            ),
            Arguments.of(
                ChangePasswordDto("validPassword", null, null),
                "validEncodedPassword",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "abc", null),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, null, "abc"),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "abc", "abc"),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "abc", "acb"),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "abcdefgh", "abcdefgh"),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "abcdefgH", "abcdefgH"),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "abc3efgH", "abc3efgH"),
                "",
                false,
                2
            ),
            Arguments.of(
                ChangePasswordDto(null, "a*c3efgH", "a*c3efgH"),
                "",
                false,
                1
            ),
            Arguments.of(
                ChangePasswordDto("validPassword", "a*c3efgH", "a*c3efgH"),
                "validEncodedPassword",
                true,
                0
            )
        )

        @JvmStatic
        fun updateAccountValidationDataProvider() = listOf(
            Arguments.of(
                1,
                AccountUpdateDto(null, null, null, null, null, null, null, null, null, null),
                false,
                9
            ),
            Arguments.of(
                2,
                AccountUpdateDto(
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
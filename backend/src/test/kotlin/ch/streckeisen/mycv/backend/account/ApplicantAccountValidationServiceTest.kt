package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.assertValidationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val EXISTING_USER_EMAIL = "existing.user@example.com"
private const val EXISTING_USERNAME = "existingUsername"

class ApplicantAccountValidationServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var messagesService: MessagesService
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService

    @BeforeTest
    fun setup() {
        applicantAccountRepository = mockk {
            every { findByUsername(eq(EXISTING_USERNAME)) } returns Optional.of(existingApplicant())
            every { findByUsername(not(eq(EXISTING_USERNAME))) } returns Optional.empty()

            every { findByEmail(eq(EXISTING_USER_EMAIL)) } returns Optional.of(existingApplicant())
            every { findByEmail(not(eq(EXISTING_USER_EMAIL))) } returns Optional.empty()
        }

        messagesService = mockk(relaxed = true) {
            every { getSupportedLanguages() } returns listOf("en", "de")
        }

        applicantAccountValidationService =
            ApplicantAccountValidationService(applicantAccountRepository, messagesService)
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

    @ParameterizedTest
    @MethodSource("validateUsernameTestDataProvider")
    fun validateUsername(value: String?, updateId: Long?, isValid: Boolean) {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateUsername(value, updateId, validationErrorBuilder)

        assertEquals(isValid, !validationErrorBuilder.hasErrors())
    }

    @Test
    fun testValidateFirstNameTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateFirstName("f".repeat(FIRST_NAME_MAX_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateFirstNameValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateFirstName("f", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLastNameTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLastName("f".repeat(LAST_NAME_MAX_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLastNameValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLastName("l", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @ParameterizedTest
    @MethodSource("validateEmailTestDataProvider")
    fun testValidateEmail(value: String?, updateId: Long?, isValid: Boolean) {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateEmail(value, updateId, validationErrorBuilder)

        assertEquals(isValid, !validationErrorBuilder.hasErrors())
    }

    @Test
    fun testValidateBirthdayEmpty() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateBirthday(null, validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateBirthdayInFuture() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateBirthday(LocalDate.now().plusDays(1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateBirthdayValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateBirthday(LocalDate.of(1985, 6, 25), validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @ParameterizedTest
    @MethodSource("validatePhoneTestDataProvider")
    fun testValidatePhone(value: String?, country: String?, isValid: Boolean) {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validatePhone(value, country, validationErrorBuilder)

        assertEquals(isValid, !validationErrorBuilder.hasErrors())
    }

    @Test
    fun testValidateStreetBlank() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateStreet("", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateStreetTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateStreet("s".repeat(STREET_MAX_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateStreetValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateStreet("street", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateHouseNumberBlank() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateHouseNumber("", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateHouseNumberNull() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateHouseNumber(null, validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateHouseNumberTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateHouseNumber("h".repeat(HOUSE_NUMBER_MAX_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateHouseNumberValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateHouseNumber("12a", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidatePostcodeBlank() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validatePostcode("", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidatePostcodeTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validatePostcode("p".repeat(POSTCODE_MAX_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidatePostcodeValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validatePostcode("1234E", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCityBlank() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCity(null, validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCityTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCity("c".repeat(CITY_MAX_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCityValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCity("city", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCountryBlank() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCountry("", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCountryTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCountry("c".repeat(COUNTRY_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCountryTooShort() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCountry("c".repeat(COUNTRY_LENGTH - 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCountryInvalid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCountry("XY", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCountryValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateCountry("DE", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLanguageBlank() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLanguage("", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLanguageTooLong() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLanguage("l".repeat(LANGUAGE_LENGTH + 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLanguageTooShort() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLanguage("l".repeat(LANGUAGE_LENGTH - 1), validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLanguageInvalid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLanguage("XY", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateLanguageValid() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateLanguage("en", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    private fun existingApplicant(): ApplicantAccountEntity = ApplicantAccountEntity(
        EXISTING_USERNAME,
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
            "CH",
            "de"
        ),
        id = 1
    )

    companion object {
        @JvmStatic
        fun validateUsernameTestDataProvider() = listOf(
            Arguments.of(
                null,
                null,
                false
            ),
            Arguments.of(
                EXISTING_USERNAME,
                null,
                false
            ),
            Arguments.of(
                EXISTING_USERNAME,
                5L,
                false
            ),
            Arguments.of(
                EXISTING_USERNAME,
                1L,
                true
            ),
            Arguments.of(
                "u".repeat(USERNAME_MAX_LENGTH + 1),
                null,
                false
            ),
            Arguments.of(
                "u",
                1L,
                true
            ),
            Arguments.of(
                "u",
                null,
                true
            )
        )

        @JvmStatic
        fun validateEmailTestDataProvider() = listOf(
            Arguments.of(
                null,
                null,
                false
            ),
            Arguments.of(
              "eeeeeeeeeeeeee",
                null,
                false
            ),
            Arguments.of(
                "e".repeat(63) + "@gm" + "a".repeat(EMAIL_MAX_LENGTH - 63) + "il.com",
                null,
                false
            ),
            Arguments.of(
                "em@il.com",
                null,
                true
            ),
            Arguments.of(
                "em@il.com",
                1L,
                true
            ),
            Arguments.of(
                EXISTING_USER_EMAIL,
                null,
                false
            ),
            Arguments.of(
                EXISTING_USER_EMAIL,
                1L,
                true
            ),
            Arguments.of(
                EXISTING_USER_EMAIL,
                2L,
                false
            )
        )

        @JvmStatic
        fun validatePhoneTestDataProvider() = listOf(
            Arguments.of(
                null,
                null,
                false
            ),
            Arguments.of(
                "",
                "",
                false
            ),
            Arguments.of(
              "p".repeat(PHONE_MAX_LENGTH + 1),
                "CH",
                false
            ),
            Arguments.of(
                "abc",
                "def",
                false
            ),
            Arguments.of(
                "abc",
                "CH",
                false
            ),
            Arguments.of(
              "+41 12 123 456 789",
                "CH",
                false
            ),
            Arguments.of(
                "+41 79 123 45 67",
                "CH",
                true
            )
        )

        @JvmStatic
        fun updateAccountValidationDataProvider() = listOf(
            Arguments.of(
                1,
                AccountUpdateDto(null, null, null, null, null, null, null, null, null, null, null, null),
                false,
                11
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
                    "CH",
                    "de"
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
                    "CH",
                    "de"
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
                    "CH",
                    "de"
                ),
                true,
                0
            ),
        )
    }
}
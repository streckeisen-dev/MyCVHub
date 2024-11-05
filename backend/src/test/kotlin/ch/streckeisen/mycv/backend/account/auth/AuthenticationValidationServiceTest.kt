package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.ApplicantAccountValidationService
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthenticationValidationServiceTest {
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationValidationService: AuthenticationValidationService

    @BeforeEach
    fun setup() {
        applicantAccountValidationService = mockk {
            every { validateFirstName(eq("valid"), any()) } just Runs
            every { validateFirstName(eq("invalid"), any()) } answers { mockValidationError("firstName", secondArg()) }
            every { validateLastName(eq("valid"), any()) } just Runs
            every { validateLastName(eq("invalid"), any()) } answers { mockValidationError("lastName", secondArg()) }
            every { validateEmail(eq("valid"), any(), any()) } just Runs
            every { validateEmail(eq("invalid"), any(), any()) } answers { mockValidationError("email", thirdArg()) }
            every { validatePhone(eq("valid"), any(), any()) } just Runs
            every { validatePhone(eq("invalid"), any(), any()) } answers { mockValidationError("phone", thirdArg()) }
            every { validateBirthday(any(), any()) } just Runs
            every { validateBirthday(isNull(), any()) } answers { mockValidationError("birthday", secondArg()) }
            every { validateStreet(eq("valid"), any()) } just Runs
            every { validateStreet(eq("invalid"), any()) } answers { mockValidationError("street", secondArg()) }
            every { validateHouseNumber(eq("valid"), any()) } just Runs
            every { validateHouseNumber(eq("invalid"), any()) } answers {
                mockValidationError(
                    "houseNumber",
                    secondArg()
                )
            }
            every { validatePostcode(eq("valid"), any()) } just Runs
            every { validatePostcode(eq("invalid"), any()) } answers { mockValidationError("postcode", secondArg()) }
            every { validateCity(eq("valid"), any()) } just Runs
            every { validateCity(eq("invalid"), any()) } answers { mockValidationError("city", secondArg()) }
            every { validateCountry(eq("valid"), any()) } just Runs
            every { validateCountry(eq("invalid"), any()) } answers { mockValidationError("country", secondArg()) }
        }
        passwordEncoder = mockk {
            every { matches(any(), any()) } returns false
            every { matches(eq("validPassword"), eq("validEncodedPassword")) } returns true
        }
        authenticationValidationService =
            AuthenticationValidationService(mockk(relaxed = true), applicantAccountValidationService, passwordEncoder)
    }

    @ParameterizedTest
    @MethodSource("signupValidationDataProvider")
    fun testValidateSignupRequest(signupRequest: SignupRequestDto, isValid: Boolean, numberOfErrors: Int) {
        val validationResult = authenticationValidationService.validateSignupRequest(signupRequest)
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
            authenticationValidationService.validateChangePasswordRequest(changePasswordRequest, currentPassword)
        assertValidationResult(result, isValid, numberOfErrors)
    }

    @ParameterizedTest
    @MethodSource("loginRequestValidationDataProvider")
    fun testValidateLoginRequest(loginRequest: LoginRequestDto, isValid: Boolean, numberOfErrors: Int) {
        val result = authenticationValidationService.validateLoginRequest(loginRequest)

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

    private fun mockValidationError(field: String, validationErrorBuilder: ValidationException.ValidationErrorBuilder) {
        validationErrorBuilder.addError(field, "error")
    }

    companion object {
        @JvmStatic
        fun loginRequestValidationDataProvider() = listOf(
            Arguments.of(LoginRequestDto(null, null), false, 2),
            Arguments.of(LoginRequestDto("username", null), false, 1),
            Arguments.of(LoginRequestDto(null, "password"), false, 1),
            Arguments.of(LoginRequestDto("username", "password"), true, 0)
        )

        @JvmStatic
        fun signupValidationDataProvider() = listOf(
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                12
            ),
            Arguments.of(
                SignupRequestDto(
                    "valid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "valid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "valid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "valid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    LocalDate.now(),
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "valid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "valid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "valid",
                    "invalid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "valid",
                    "invalid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    null,
                    "invalid",
                    "invalid",
                    "invalid",
                    "invalid",
                    "valid",
                    null,
                    null
                ),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(
                    "valid",
                    "valid",
                    "valid",
                    "valid",
                    LocalDate.now(),
                    "valid",
                    "valid",
                    "valid",
                    "valid",
                    "valid",
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
    }
}
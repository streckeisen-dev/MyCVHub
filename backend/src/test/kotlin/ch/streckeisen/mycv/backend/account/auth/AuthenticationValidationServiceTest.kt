package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.ApplicantAccountValidationService
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.util.assertValidationResult
import ch.streckeisen.mycv.backend.util.executeParameterizedTest
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

private const val VALID_ATTRIBUTE = "valid"
private const val INVALID_ATTRIBUTE = "invalid"
private const val VALID_TEST_PW = "a*c3efgH"

class AuthenticationValidationServiceTest {
    private lateinit var applicantAccountValidationService: ApplicantAccountValidationService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationValidationService: AuthenticationValidationService

    @BeforeEach
    fun setup() {
        applicantAccountValidationService = mockk {
            every { validateUsername(eq(VALID_ATTRIBUTE), isNull(), any()) } just Runs
            every { validateUsername(eq(INVALID_ATTRIBUTE), any(), any()) } answers {
                mockValidationError(
                    "username",
                    thirdArg()
                )
            }
            every { validateFirstName(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateFirstName(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "firstName",
                    secondArg()
                )
            }
            every { validateLastName(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateLastName(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "lastName",
                    secondArg()
                )
            }
            every { validateEmail(eq(VALID_ATTRIBUTE), any(), any()) } just Runs
            every {
                validateEmail(
                    eq(INVALID_ATTRIBUTE),
                    any(),
                    any()
                )
            } answers { mockValidationError("ch/streckeisen/mycv/email", thirdArg()) }
            every { validatePhone(eq(VALID_ATTRIBUTE), any(), any()) } just Runs
            every { validatePhone(eq(INVALID_ATTRIBUTE), any(), any()) } answers {
                mockValidationError(
                    "phone",
                    thirdArg()
                )
            }
            every { validateBirthday(any(), any()) } just Runs
            every { validateBirthday(isNull(), any()) } answers { mockValidationError("birthday", secondArg()) }
            every { validateStreet(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateStreet(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "street",
                    secondArg()
                )
            }
            every { validateHouseNumber(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateHouseNumber(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "houseNumber",
                    secondArg()
                )
            }
            every { validatePostcode(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validatePostcode(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "postcode",
                    secondArg()
                )
            }
            every { validateCity(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateCity(eq(INVALID_ATTRIBUTE), any()) } answers { mockValidationError("city", secondArg()) }
            every { validateCountry(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateCountry(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "country",
                    secondArg()
                )
            }

            every { validateLanguage(eq(VALID_ATTRIBUTE), any()) } just Runs
            every { validateLanguage(eq(INVALID_ATTRIBUTE), any()) } answers {
                mockValidationError(
                    "language",
                    secondArg()
                )
            }
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
        executeParameterizedTest(signupRequest, isValid, numberOfErrors) {
            authenticationValidationService.validateSignupRequest(it)
        }
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
        executeParameterizedTest(loginRequest, isValid, numberOfErrors) {
            authenticationValidationService.validateLoginRequest(it)
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
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                14
            ),
            Arguments.of(
                SignupRequestDto(
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    LocalDate.now(),
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    null,
                    null,
                    INVALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    INVALID_ATTRIBUTE,
                    null,
                    null,
                    VALID_ATTRIBUTE
                ),
                false,
                13
            ),
            Arguments.of(
                SignupRequestDto(
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    LocalDate.now(),
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_ATTRIBUTE,
                    VALID_TEST_PW,
                    VALID_TEST_PW,
                    VALID_ATTRIBUTE
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
                ChangePasswordDto(null, VALID_TEST_PW, VALID_TEST_PW),
                "",
                false,
                1
            ),
            Arguments.of(
                ChangePasswordDto("validPassword", VALID_TEST_PW, VALID_TEST_PW),
                "validEncodedPassword",
                true,
                0
            )
        )
    }
}
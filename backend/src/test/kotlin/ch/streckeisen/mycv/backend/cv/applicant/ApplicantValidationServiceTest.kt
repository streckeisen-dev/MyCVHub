package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.account.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val EXISTING_USER_EMAIL = "existing.user@example.com"

class ApplicantValidationServiceTest {
    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var applicantValidationService: ApplicantValidationService

    @BeforeTest
    fun setup() {
        applicantRepository = mockk()
        every { applicantRepository.findByEmail(eq(EXISTING_USER_EMAIL)) } returns Optional.of(existingApplicant())
        every { applicantRepository.findByEmail(not(eq(EXISTING_USER_EMAIL))) } returns Optional.empty()

        applicantValidationService = ApplicantValidationService(applicantRepository)
    }

    @ParameterizedTest
    @MethodSource("applicantValidationServiceProvider")
    fun testApplicantValidation(signupRequest: SignupRequestDto, isValid: Boolean, numberOfErrors: Int?) {
        val validationResult = applicantValidationService.validateSignupRequest(signupRequest)
        if (isValid) {
            assertTrue { validationResult.isSuccess }
            assertNull(validationResult.exceptionOrNull())
        } else {
            assertTrue { validationResult.isFailure }
            val throwable = validationResult.exceptionOrNull()
            assertNotNull(throwable)
            assertTrue(throwable is ValidationException)
            assertNotNull(throwable.errors)
            assertEquals(numberOfErrors, throwable.errors.size)
        }
    }

    private fun existingApplicant(): Applicant = Applicant(
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
        1
    )

    companion object {
        @JvmStatic
        fun applicantValidationServiceProvider() = listOf(
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, null),
                false,
                10
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
                10
            ),
            Arguments.of(
                SignupRequestDto("FirstName", null, null, null, null, null, null, null, null, null, null, null),
                false,
                9
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
                10
            ),
            Arguments.of(
                SignupRequestDto(null, "LastName", null, null, null, null, null, null, null, null, null, null),
                false,
                9
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
                10
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
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, "f.l@e.c", null, null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, EXISTING_USER_EMAIL, null, null, null, null, null, null, null, null, null),
                false,
                10
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
                9
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, "abcd", null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, "123456", null, null, null, null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, "+41 79 123 45 67", null, null, null, null, null, null, null, null),
                false,
                9
            ),
            Arguments.of(
                SignupRequestDto(
                    null,
                    null,
                    null,
                    null,
                    LocalDate.now().minusDays(2),
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
                9
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
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, "StreetName", null, null, null, null, null, null),
                false,
                9
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, "12345678901", null, null, null, null, null),
                false,
                11
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, "226a", null, null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, "1234567890123456", null, null, null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, "8000", null, null, null, null),
                false,
                9
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
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, "City", null, null, null),
                false,
                9
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, "GER", null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, "XY", null, null),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, "CH", null, null),
                false,
                9
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, "abc"),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, "abcdefgh"),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, "abcdefgH"),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, "abc3efgH"),
                false,
                10
            ),
            Arguments.of(
                SignupRequestDto(null, null, null, null, null, null, null, null, null, null, null, "a*c3efgH"),
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
                    true,
                    "a*c3efgH"
                ),
                true,
                0
            )
        )
    }
}
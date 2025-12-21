package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.executeParameterizedTest
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class EducationValidationServiceTest {
    private lateinit var educationValidationService: EducationValidationService

    @BeforeEach
    fun setup() {
        val messagesService: MessagesService = mockk(relaxed = true)
        educationValidationService = EducationValidationService(StringValidator(messagesService), messagesService)
    }

    @ParameterizedTest
    @MethodSource("educationValidationDataProvider")
    fun testValidateEduction(educationUpdate: EducationUpdateDto, isValid: Boolean, numberOfErrors: Int) {
        executeParameterizedTest(
            educationUpdate,
            isValid,
            numberOfErrors
        ) { educationValidationService.validateEducation(educationUpdate) }
    }

    companion object {
        @JvmStatic
        fun educationValidationDataProvider() = listOf(
            Arguments.of(
                EducationUpdateDto(null, null, null, null, null, null, null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, "i".repeat(INSTITUTION_MAX_LENGTH + 1), null, null, null, null, null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, "institution", null, null, null, null, null),
                false,
                3
            ),
            Arguments.of(
                EducationUpdateDto(null, null, "l".repeat(LOCATION_MAX_LENGTH + 1), null, null, null, null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, null, "location", null, null, null, null),
                false,
                3
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, LocalDate.now().plusDays(1), null, null, null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, LocalDate.of(2024, 2, 15), null, null, null),
                false,
                3
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, null, LocalDate.now().plusDays(1), null, null),
                false,
                5
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, null, LocalDate.of(2024, 2, 14), null, null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 14), null, null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 16), null, null),
                false,
                3
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, null, null, "d".repeat(DEGREE_NAME_MAX_LENGTH + 1), null),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, null, null, "degreeName", null),
                false,
                3
            ),
            Arguments.of(
                EducationUpdateDto(null, null, null, null, null, null, "description"),
                false,
                4
            ),
            Arguments.of(
                EducationUpdateDto(
                    null,
                    "institution",
                    "location",
                    LocalDate.of(2024, 2, 15),
                    LocalDate.of(2024, 2, 16),
                    "degreeName",
                    "description"
                ),
                true,
                0
            ),
            Arguments.of(
                EducationUpdateDto(
                    null,
                    "institution",
                    "location",
                    LocalDate.of(2024, 2, 15),
                    LocalDate.of(2024, 2, 16),
                    "degreeName",
                    null
                ),
                true,
                0
            )
        )
    }
}
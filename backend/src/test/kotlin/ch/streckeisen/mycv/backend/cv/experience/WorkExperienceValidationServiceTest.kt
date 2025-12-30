package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.executeParameterizedTest
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class WorkExperienceValidationServiceTest {
    private lateinit var workExperienceValidationService: WorkExperienceValidationService

    @BeforeEach
    fun setup() {
        val messagesService: MessagesService = mockk(relaxed = true)
        workExperienceValidationService =
            WorkExperienceValidationService(StringValidator(messagesService), messagesService)
    }

    @ParameterizedTest
    @MethodSource("workExperienceValidationDataProvider")
    fun testValidateWorkExperience(
        workExperienceUpdate: WorkExperienceUpdateDto,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        executeParameterizedTest(
            workExperienceUpdate,
            isValid,
            numberOfErrors
        ) { workExperienceValidationService.validateWorkExperience(workExperienceUpdate) }
    }

    @Test
    fun testValidateWorkExperienceAllowingFutureStart() {
        val workExperienceUpdate = WorkExperienceUpdateDto(
            null,
            "Job",
            "Location",
            "Company",
            LocalDate.now().plusDays(1),
            null,
            "description"
        )
        val result = workExperienceValidationService.validateWorkExperience(workExperienceUpdate, true)

        assertTrue(result.isSuccess)
    }

    companion object {
        @JvmStatic
        fun workExperienceValidationDataProvider() = listOf(
            Arguments.of(WorkExperienceUpdateDto(null, null, null, null, null, null, null), false, 5),
            Arguments.of(
                WorkExperienceUpdateDto(null, "j".repeat(JOB_TITLE_MAX_LENGTH + 1), null, null, null, null, null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, "jobTitle", null, null, null, null, null),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, "l".repeat(LOCATION_MAX_LENGTH + 1), null, null, null, null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, "location", null, null, null, null),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, "c".repeat(COMPANY_MAX_LENGTH + 1), null, null, null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, "company", null, null, null),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null, LocalDate.now().plusDays(1), null, null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null, LocalDate.of(2024, 1, 2), null, null),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null, null, LocalDate.now().plusDays(1), null),
                false,
                6
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null, null, LocalDate.of(2024, 1, 1), null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(
                    null,
                    null,
                    null,
                    null,
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 1),
                    null
                ),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(
                    null,
                    null,
                    null,
                    null,
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 30),
                    null
                ),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null, null, null, "desc-\nription"),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(
                    null,
                    "Job",
                    "Location",
                    "Company",
                    LocalDate.of(2024, 1, 2),
                    null,
                    "desc-\nription"
                ),
                true,
                0
            ),
        )
    }
}
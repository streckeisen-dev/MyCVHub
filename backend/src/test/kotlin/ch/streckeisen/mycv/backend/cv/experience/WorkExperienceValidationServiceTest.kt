package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorkExperienceValidationServiceTest {
    private lateinit var workExperienceValidationService: WorkExperienceValidationService

    @BeforeEach
    fun setup() {
        workExperienceValidationService = WorkExperienceValidationService(mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("workExperienceValidationDataProvider")
    fun testValidateWorkExperience(
        workExperienceUpdate: WorkExperienceUpdateDto,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        val result = workExperienceValidationService.validateWorkExperience(workExperienceUpdate)

        assertEquals(isValid, result.isSuccess)
        if (!isValid) {
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is ValidationException)
            assertEquals(numberOfErrors, ex.errors.size)
        }
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
                WorkExperienceUpdateDto(null, null, null, null,  null, LocalDate.now().plusDays(1), null),
                false,
                6
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null,  null, LocalDate.of(2024, 1, 1), null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null,  LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 1), null),
                false,
                5
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null,  LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 30), null),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, null, null, null,  null, null, "desc-\nription"),
                false,
                4
            ),
            Arguments.of(
                WorkExperienceUpdateDto(null, "Job", "Location", "Company",  LocalDate.of(2024, 1, 2), null, "desc-\nription"),
                true,
                0
            ),
        )
    }
}
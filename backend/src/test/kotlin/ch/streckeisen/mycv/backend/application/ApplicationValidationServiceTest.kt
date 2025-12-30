package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.application.dto.ScheduledWorkExperienceDto
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceValidationService
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.executeParameterizedTest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

val validScheduledWorkExperience = ScheduledWorkExperienceDto("job", "loc", "comp", LocalDate.now().plusDays(5), "desc")

class ApplicationValidationServiceTest {
    private lateinit var workExperienceValidationService: WorkExperienceValidationService
    private lateinit var validationService: ApplicationValidationService

    @BeforeEach
    fun setup() {
        val messagesService: MessagesService = mockk(relaxed = true)
        workExperienceValidationService = mockk {
            every {
                validateWorkExperience(
                    any(),
                    any()
                )
            } returns Result.failure(
                ValidationException.ValidationErrorBuilder()
                    .addError("jobTitle", "error")
                    .build("msg")
            )

            every {
                validateWorkExperience(
                    eq(validScheduledWorkExperience.toUpdateRequest()),
                    eq(true)
                )
            } returns Result.success(Unit)
        }
        validationService = ApplicationValidationService(
            StringValidator(messagesService),
            messagesService,
            workExperienceValidationService
        )
    }

    @ParameterizedTest
    @MethodSource("applicationValidationDataProvider")
    fun testValidateUpdate(updateRequest: ApplicationUpdateDto, isValid: Boolean, numberOfErrors: Int) {
        executeParameterizedTest(updateRequest, isValid, numberOfErrors) { validationService.validateUpdate(it) }
    }

    @ParameterizedTest
    @MethodSource("transitionValidationDataProvider")
    fun testValidateTransition(
        transitionRequest: ApplicationTransitionRequestDto,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        executeParameterizedTest(
            transitionRequest,
            isValid,
            numberOfErrors
        ) { validationService.validateTransition(it) }
    }

    @Test
    fun testValidateTransitionWithInvalidScheduledWorkExperience() {
        val request =
            ApplicationTransitionRequestDto(1, "abc", ScheduledWorkExperienceDto(null, null, null, null, null))

        val result = validationService.validateTransition(request)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ValidationException }
        assertTrue { (ex as ValidationException).errors.isNotEmpty() }
    }

    @Test
    fun testValidateTransitionWithValidScheduledWorkExperience() {
        val request = ApplicationTransitionRequestDto(1, "abc", validScheduledWorkExperience)

        val result = validationService.validateTransition(request)

        assertTrue { result.isSuccess }
    }

    companion object {
        @JvmStatic
        fun applicationValidationDataProvider() = listOf(
            Arguments.of(
                ApplicationUpdateDto(null, null, null, null, null),
                false,
                2
            ),
            Arguments.of(
                ApplicationUpdateDto(null, "t".repeat(JOB_TITLE_MAX_LENGTH + 1), null, null, null),
                false,
                2
            ),
            Arguments.of(
                ApplicationUpdateDto(null, "t".repeat(JOB_TITLE_MAX_LENGTH - 1), null, null, null),
                false,
                1
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, "c".repeat(COMPANY_MAX_LENGTH + 1), null, null),
                false,
                2
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, "c".repeat(COMPANY_MAX_LENGTH - 1), null, null),
                false,
                1
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, null, "", null),
                false,
                3
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, null, "s", null),
                false,
                3
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, null, "https://example.com", null),
                false,
                2
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, null, null, ""),
                false,
                3
            ),
            Arguments.of(
                ApplicationUpdateDto(null, null, null, null, "d"),
                false,
                2
            ),
            Arguments.of(
                ApplicationUpdateDto(null, "t", "c", "https://example.com", "d"),
                true,
                0
            ),
            Arguments.of(
                ApplicationUpdateDto(null, "t", "c", null, null),
                true,
                0
            )
        )

        @JvmStatic
        fun transitionValidationDataProvider() = listOf(
            Arguments.of(ApplicationTransitionRequestDto(1, "", null), false, 1),
            Arguments.of(ApplicationTransitionRequestDto(1, null, null), true, 0),
            Arguments.of(ApplicationTransitionRequestDto(1, "comment", null), true, 0)
        )
    }
}
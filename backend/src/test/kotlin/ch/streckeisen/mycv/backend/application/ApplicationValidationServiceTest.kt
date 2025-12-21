package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.executeParameterizedTest
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ApplicationValidationServiceTest {
    private lateinit var validationService: ApplicationValidationService

    @BeforeEach
    fun setup() {
        val messagesService: MessagesService = mockk(relaxed = true)
        validationService = ApplicationValidationService(StringValidator(messagesService), messagesService)
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
            Arguments.of(ApplicationTransitionRequestDto(1, ""), false, 1),
            Arguments.of(ApplicationTransitionRequestDto(1, null), true, 0),
            Arguments.of(ApplicationTransitionRequestDto(1, "comment"), true, 0)
        )
    }
}
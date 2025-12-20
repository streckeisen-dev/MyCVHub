package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.isUrlValid
import org.springframework.stereotype.Service

private const val UPDATE_VALIDATION_ERROR_KEY = "$MYCV_KEY_PREFIX.application.validation.updateError"
private const val TRANSITION_VALIDATION_ERROR_KEY = "$MYCV_KEY_PREFIX.application.validation.transitionError"

@Service
class ApplicationValidationService(
    private val stringValidator: StringValidator,
    private val messagesService: MessagesService
) {
    fun validateUpdate(update: ApplicationUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            requiredField = "jobTitle",
            value = update.jobTitle,
            maxLength = JOB_TITLE_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = "company",
            value = update.company,
            maxLength = COMPANY_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateOptionalString(
            optionalField = "source",
            value = update.source,
            validationErrorBuilder = validationErrorBuilder
        )
        if (!update.source.isNullOrBlank() && !isUrlValid(update.source)) {
            val error = messagesService.invalidUrlError(update.source)
            validationErrorBuilder.addError("source", error)
        }

        stringValidator.validateOptionalString(
            optionalField = "description",
            value = update.description,
            validationErrorBuilder = validationErrorBuilder
        )

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(UPDATE_VALIDATION_ERROR_KEY))
        }
        return Result.success(Unit)
    }

    fun validateTransition(transitionRequest: ApplicationTransitionRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateOptionalString(
            optionalField = "comment",
            value = transitionRequest.comment,
            maxLength = TRANSITION_COMMENT_MAX_LENGTH,
            validationErrorBuilder
        )

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(TRANSITION_VALIDATION_ERROR_KEY))
        }

        return Result.success(Unit)
    }
}
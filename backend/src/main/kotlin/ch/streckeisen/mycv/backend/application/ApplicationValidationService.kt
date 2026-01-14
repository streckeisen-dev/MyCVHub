package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceValidationService
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.isUrlValid
import org.springframework.stereotype.Service

private const val UPDATE_VALIDATION_ERROR_KEY = "$MYCV_KEY_PREFIX.application.validation.updateError"
private const val TRANSITION_VALIDATION_ERROR_KEY = "$MYCV_KEY_PREFIX.application.validation.transitionError"

private const val JOB_TITLE_FIELD_KEY = "jobTitle"
private const val COMPANY_FIELD_KEY = "company"
private const val SOURCE_FIELD_KEY = "source"
private const val DESCRIPTION_FIELD_KEY = "description"
private const val COMMENT_FIELD_KEY = "comment"

@Service
class ApplicationValidationService(
    private val stringValidator: StringValidator,
    private val messagesService: MessagesService,
    private val workExperienceValidationService: WorkExperienceValidationService
) {
    fun validateUpdate(update: ApplicationUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            requiredField = JOB_TITLE_FIELD_KEY,
            value = update.jobTitle,
            maxLength = JOB_TITLE_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = COMPANY_FIELD_KEY,
            value = update.company,
            maxLength = COMPANY_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateOptionalString(
            optionalField = SOURCE_FIELD_KEY,
            value = update.source,
            validationErrorBuilder = validationErrorBuilder
        )
        if (!update.source.isNullOrBlank() && !isUrlValid(update.source)) {
            val error = messagesService.invalidUrlError(update.source)
            validationErrorBuilder.addError(SOURCE_FIELD_KEY, error)
        }

        stringValidator.validateOptionalString(
            optionalField = DESCRIPTION_FIELD_KEY,
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
            optionalField = COMMENT_FIELD_KEY,
            value = transitionRequest.comment,
            maxLength = TRANSITION_COMMENT_MAX_LENGTH,
            validationErrorBuilder
        )

        if (transitionRequest.scheduledWorkExperience != null) {
            val updatedRequest = transitionRequest.scheduledWorkExperience.toUpdateRequest()
            workExperienceValidationService.validateWorkExperience(updatedRequest, allowFutureStart = true)
                .onFailure { ex ->
                    (ex as ValidationException).errors.forEach { validationErrorBuilder.addError(it.key, it.value) }
                }
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(TRANSITION_VALIDATION_ERROR_KEY))
        }

        return Result.success(Unit)
    }
}
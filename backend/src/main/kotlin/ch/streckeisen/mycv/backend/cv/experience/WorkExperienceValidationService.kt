package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import org.springframework.stereotype.Service
import java.time.LocalDate

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.workExperience.validationError"
private const val JOB_TITLE_FIELD_KEY = "jobTitle"
private const val LOCATION_FIELD_KEY = "location"
private const val COMPANY_FIELD_KEY = "company"
private const val POSITION_START_FIELD_KEY = "positionStart"
private const val POSITION_END_FIELD_KEY = "positionEnd"
private const val DESCRIPTION_FIELD_KEY = "description"

@Service
class WorkExperienceValidationService(
    private val stringValidator: StringValidator,
    private val messagesService: MessagesService
) {
    fun validateWorkExperience(
        workExperience: WorkExperienceUpdateDto,
        allowFutureStart: Boolean = false
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            requiredField = JOB_TITLE_FIELD_KEY,
            value = workExperience.jobTitle,
            maxLength = JOB_TITLE_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = LOCATION_FIELD_KEY,
            value = workExperience.location,
            maxLength = LOCATION_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = COMPANY_FIELD_KEY,
            value = workExperience.company,
            maxLength = COMPANY_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        if (workExperience.positionStart == null) {
            val error = messagesService.requiredFieldMissingError(POSITION_START_FIELD_KEY)
            validationErrorBuilder.addError(POSITION_START_FIELD_KEY, error)
        } else if (!allowFutureStart && workExperience.positionStart.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(POSITION_START_FIELD_KEY)
            validationErrorBuilder.addError(POSITION_START_FIELD_KEY, error)
        }

        if (workExperience.positionEnd != null && workExperience.positionEnd.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(POSITION_END_FIELD_KEY)
            validationErrorBuilder.addError(POSITION_END_FIELD_KEY, error)
        } else if (
            workExperience.positionEnd != null
            && workExperience.positionStart != null
            && workExperience.positionEnd.isBefore(workExperience.positionStart)
        ) {
            val error = messagesService.dateIsAfterError(POSITION_START_FIELD_KEY, POSITION_END_FIELD_KEY)
            validationErrorBuilder.addError(POSITION_END_FIELD_KEY, error)
        }

        stringValidator.validateRequiredString(
            requiredField = DESCRIPTION_FIELD_KEY,
            value = workExperience.description,
            validationErrorBuilder = validationErrorBuilder
        )

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
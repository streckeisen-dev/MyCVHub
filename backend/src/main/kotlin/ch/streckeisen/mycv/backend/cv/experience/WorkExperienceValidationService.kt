package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
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
    private val messagesService: MessagesService
) {
    fun validateWorkExperience(workExperience: WorkExperienceUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (workExperience.jobTitle.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(JOB_TITLE_FIELD_KEY)
            validationErrorBuilder.addError(JOB_TITLE_FIELD_KEY, error)
        } else if (workExperience.jobTitle.length > JOB_TITLE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(JOB_TITLE_FIELD_KEY, JOB_TITLE_MAX_LENGTH)
            validationErrorBuilder.addError(JOB_TITLE_FIELD_KEY, error)
        }

        if (workExperience.location.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(LOCATION_FIELD_KEY)
            validationErrorBuilder.addError(LOCATION_FIELD_KEY, error)
        } else if (workExperience.location.length > LOCATION_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(LOCATION_FIELD_KEY, LOCATION_MAX_LENGTH)
            validationErrorBuilder.addError(LOCATION_FIELD_KEY, error)
        }

        if (workExperience.company.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(COMPANY_FIELD_KEY)
            validationErrorBuilder.addError(COMPANY_FIELD_KEY, error)
        } else if (workExperience.company.length > COMPANY_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(LOCATION_FIELD_KEY, LOCATION_MAX_LENGTH)
            validationErrorBuilder.addError(COMPANY_FIELD_KEY, error)
        }

        if (workExperience.positionStart == null) {
            val error = messagesService.requiredFieldMissingError(POSITION_START_FIELD_KEY)
            validationErrorBuilder.addError(POSITION_START_FIELD_KEY, error)
        } else if (workExperience.positionStart.isAfter(LocalDate.now())) {
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

        if (workExperience.description.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(DESCRIPTION_FIELD_KEY)
            validationErrorBuilder.addError(DESCRIPTION_FIELD_KEY, error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
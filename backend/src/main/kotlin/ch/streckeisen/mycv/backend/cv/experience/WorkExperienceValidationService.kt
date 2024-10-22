package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service
import java.time.LocalDate

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.workExperience.validationError"

@Service
class WorkExperienceValidationService(
    private val messagesService: MessagesService
) {
    fun validateWorkExperience(workExperience: WorkExperienceUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (workExperience.jobTitle.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("jobTitle")
            validationErrorBuilder.addError("jobTitle", error)
        } else if (workExperience.jobTitle.length > JOB_TITLE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("jobTitle", JOB_TITLE_MAX_LENGTH)
            validationErrorBuilder.addError("jobTitle", error)
        }

        if (workExperience.location.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("location")
            validationErrorBuilder.addError("location", error)
        } else if (workExperience.location.length > LOCATION_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("location", LOCATION_MAX_LENGTH)
            validationErrorBuilder.addError("location", error)
        }

        if (workExperience.company.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("company")
            validationErrorBuilder.addError("company", error)
        } else if (workExperience.company.length > COMPANY_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("location", LOCATION_MAX_LENGTH)
            validationErrorBuilder.addError("company", error)
        }

        if (workExperience.positionStart == null) {
            val error = messagesService.requiredFieldMissingError("positionStart")
            validationErrorBuilder.addError("positionStart", error)
        } else if (workExperience.positionStart.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError("positionStart")
            validationErrorBuilder.addError("positionStart", error)
        }

        if (workExperience.positionEnd != null && workExperience.positionEnd.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError("positionEnd")
            validationErrorBuilder.addError("positionEnd", error)
        } else if (
            workExperience.positionEnd != null
            && workExperience.positionStart != null
            && workExperience.positionEnd.isBefore(workExperience.positionStart)
        ) {
            val error = messagesService.dateIsAfterError("positionStart", "positionEnd")
            validationErrorBuilder.addError("positionEnd", error)
        }

        if (workExperience.description.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("description")
            validationErrorBuilder.addError("description", error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
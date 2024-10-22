package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service
import java.time.LocalDate

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.education.validation.error"

@Service
class EducationValidationService(
    private val messagesService: MessagesService
) {
    fun validateEducation(educationUpdate: EducationUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (educationUpdate.institution.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("institution")
            validationErrorBuilder.addError("institution", error)
        } else if (educationUpdate.institution.length > INSTITUTION_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("institution", INSTITUTION_MAX_LENGTH)
            validationErrorBuilder.addError("institution", error)
        }

        if (educationUpdate.location.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("location")
            validationErrorBuilder.addError("location", error)
        } else if (educationUpdate.location.length > LOCATION_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("location", LOCATION_MAX_LENGTH)
            validationErrorBuilder.addError("location", error)
        }

        if (educationUpdate.degreeName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("degreeName")
            validationErrorBuilder.addError("degreeName", error)
        } else if (educationUpdate.degreeName.length > DEGREE_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("degreeName", DEGREE_NAME_MAX_LENGTH)
            validationErrorBuilder.addError("degreeName", error)
        }

        if (educationUpdate.educationStart == null) {
            val error = messagesService.requiredFieldMissingError("educationStart")
            validationErrorBuilder.addError("educationStart", error)
        } else if (educationUpdate.educationStart.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError("educationStart")
            validationErrorBuilder.addError("educationStart", error)
        }

        if (educationUpdate.educationEnd != null && educationUpdate.educationEnd.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError("educationEnd")
            validationErrorBuilder.addError("educationEnd", error)
        } else if (
            educationUpdate.educationEnd != null
            && educationUpdate.educationStart != null
            && educationUpdate.educationEnd.isBefore(educationUpdate.educationStart)
        ) {
            val error = messagesService.dateIsAfterError("educationStart", "educationEnd")
            validationErrorBuilder.addError("educationEnd", error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
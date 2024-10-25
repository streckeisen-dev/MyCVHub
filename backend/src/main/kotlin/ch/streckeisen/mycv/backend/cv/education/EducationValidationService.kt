package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service
import java.time.LocalDate

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.education.validation.error"
private const val INSTITUTION_FIELD_KEY = "institution"
private const val LOCATION_FIELD_KEY = "location"
private const val DEGREE_FIELD_KEY = "degreeName"
private const val EDUCATION_START_FIELD_KEY = "educationStart"
private const val EDUCATION_END_FIELD_KEY = "educationEnd"

@Service
class EducationValidationService(
    private val messagesService: MessagesService
) {
    fun validateEducation(educationUpdate: EducationUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (educationUpdate.institution.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(INSTITUTION_FIELD_KEY)
            validationErrorBuilder.addError(INSTITUTION_FIELD_KEY, error)
        } else if (educationUpdate.institution.length > INSTITUTION_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(INSTITUTION_FIELD_KEY, INSTITUTION_MAX_LENGTH)
            validationErrorBuilder.addError(INSTITUTION_FIELD_KEY, error)
        }

        if (educationUpdate.location.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(LOCATION_FIELD_KEY)
            validationErrorBuilder.addError(LOCATION_FIELD_KEY, error)
        } else if (educationUpdate.location.length > LOCATION_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(LOCATION_FIELD_KEY, LOCATION_MAX_LENGTH)
            validationErrorBuilder.addError(LOCATION_FIELD_KEY, error)
        }

        if (educationUpdate.degreeName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(DEGREE_FIELD_KEY)
            validationErrorBuilder.addError(DEGREE_FIELD_KEY, error)
        } else if (educationUpdate.degreeName.length > DEGREE_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(DEGREE_FIELD_KEY, DEGREE_NAME_MAX_LENGTH)
            validationErrorBuilder.addError(DEGREE_FIELD_KEY, error)
        }

        if (educationUpdate.educationStart == null) {
            val error = messagesService.requiredFieldMissingError(EDUCATION_START_FIELD_KEY)
            validationErrorBuilder.addError(EDUCATION_START_FIELD_KEY, error)
        } else if (educationUpdate.educationStart.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(EDUCATION_START_FIELD_KEY)
            validationErrorBuilder.addError(EDUCATION_START_FIELD_KEY, error)
        }

        if (educationUpdate.educationEnd != null && educationUpdate.educationEnd.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(EDUCATION_END_FIELD_KEY)
            validationErrorBuilder.addError(EDUCATION_END_FIELD_KEY, error)
        } else if (
            educationUpdate.educationEnd != null
            && educationUpdate.educationStart != null
            && educationUpdate.educationEnd.isBefore(educationUpdate.educationStart)
        ) {
            val error = messagesService.dateIsAfterError(EDUCATION_START_FIELD_KEY, EDUCATION_END_FIELD_KEY)
            validationErrorBuilder.addError(EDUCATION_END_FIELD_KEY, error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
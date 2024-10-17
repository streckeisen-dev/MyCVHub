package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EducationValidationService {
    fun validateEducation(educationUpdate: EducationUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (educationUpdate.institution.isNullOrBlank()) {
            validationErrorBuilder.addError("institution", "Institution must not be blank")
        } else if (educationUpdate.institution.length > INSTITUTION_MAX_LENGTH) {
            validationErrorBuilder.addError(
                "institution",
                "Institution must not exceed $INSTITUTION_MAX_LENGTH characters"
            )
        }

        if (educationUpdate.location.isNullOrBlank()) {
            validationErrorBuilder.addError("location", "Location must not be blank")
        } else if (educationUpdate.location.length > LOCATION_MAX_LENGTH) {
            validationErrorBuilder.addError("location", "Location must not exceed $LOCATION_MAX_LENGTH characters")
        }

        if (educationUpdate.degreeName.isNullOrBlank()) {
            validationErrorBuilder.addError("degreeName", "Degree Name must not be blank")
        } else if (educationUpdate.degreeName.length > DEGREE_NAME_MAX_LENGTH) {
            validationErrorBuilder.addError(
                "degreeName",
                "Degree Name must not exceed $DEGREE_NAME_MAX_LENGTH characters"
            )
        }

        if (educationUpdate.educationStart == null) {
            validationErrorBuilder.addError("educationStart", "Education Start must not be null")
        } else if (educationUpdate.educationStart.isAfter(LocalDate.now())) {
            validationErrorBuilder.addError("educationStart", "Education Start must not be in the future")
        }

        if (educationUpdate.educationEnd != null && educationUpdate.educationEnd.isAfter(LocalDate.now())) {
            validationErrorBuilder.addError("educationEnd", "Education End must not be in the future")
        } else if (
            educationUpdate.educationEnd != null
            && educationUpdate.educationStart != null
            && educationUpdate.educationEnd.isBefore(educationUpdate.educationStart)
        ) {
            validationErrorBuilder.addError("educationEnd", "Education End must not be before Education Start")
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Eduction validation failed"))
        }
        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class WorkExperienceValidationService {
    fun validateWorkExperience(workExperience: WorkExperienceUpdateRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (workExperience.jobTitle.isNullOrBlank()) {
            validationErrorBuilder.addError("jobTitle", "Job Title must not be blank")
        } else if (workExperience.jobTitle.length > JOB_TITLE_MAX_LENGTH) {
            validationErrorBuilder.addError("jobTitle", "Job Title must not exceed $JOB_TITLE_MAX_LENGTH characters")
        }

        if (workExperience.location.isNullOrBlank()) {
            validationErrorBuilder.addError("location", "Location must not be blank")
        } else if (workExperience.location.length > LOCATION_MAX_LENGTH) {
            validationErrorBuilder.addError("location", "Location must not exceed $LOCATION_MAX_LENGTH characters")
        }

        if (workExperience.company.isNullOrBlank()) {
            validationErrorBuilder.addError("company", "Company must not be blank")
        } else if (workExperience.company.length > COMPANY_MAX_LENGTH) {
            validationErrorBuilder.addError("company", "Company must not exceed $COMPANY_MAX_LENGTH characters")
        }

        if (workExperience.positionStart == null) {
            validationErrorBuilder.addError("positionStart", "Position Start must not be null")
        } else if (workExperience.positionStart.isAfter(LocalDate.now())) {
            validationErrorBuilder.addError("positionStart", "Position Start must not be in the future")
        }

        if (workExperience.positionEnd != null && workExperience.positionEnd.isBefore(workExperience.positionStart)) {
            validationErrorBuilder.addError("positionEnd", "Position End must be after Position Start")
        } else if (workExperience.positionEnd != null && workExperience.positionEnd.isAfter(LocalDate.now())) {
            validationErrorBuilder.addError("positionEnd", "Position End must be in the future")
        }

        if (workExperience.description.isNullOrBlank()) {
            validationErrorBuilder.addError("description", "Description must not be blank")
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Work experience validation failed"))
        }
        return Result.success(Unit)
    }
}
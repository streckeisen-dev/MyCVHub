package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.stereotype.Service

@Service
class ProfileValidationService(
    private val profileRepository: ProfileRepository
) {
    fun validateProfileInformation(
        updateAccountId: Long,
        profileInformationUpdate: GeneralProfileInformationUpdateDto
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (profileInformationUpdate.alias.isNullOrBlank()) {
            validationErrorBuilder.addError("alias", "Alias must not be blank")
        } else if (profileInformationUpdate.alias.length > ALIAS_MAX_LENGTH) {
            validationErrorBuilder.addError("alias", "Alias must not exceed $ALIAS_MAX_LENGTH characters")
        } else {
            val profileWithSameAlias = profileRepository.findByAlias(profileInformationUpdate.alias).orElse(null)
            if (profileWithSameAlias != null && profileWithSameAlias.account.id != updateAccountId) {
                validationErrorBuilder.addError("alias", "Alias is already taken")
            }
        }

        if (profileInformationUpdate.jobTitle.isNullOrBlank()) {
            validationErrorBuilder.addError("jobTitle", "Job Title must not be blank")
        } else if (profileInformationUpdate.jobTitle.length > JOB_TITLE_MAX_LENGTH) {
            validationErrorBuilder.addError("jobTitle", "Job Title must not exceed $JOB_TITLE_MAX_LENGTH characters")
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Failed to update profile information"))
        }

        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

val aliasRegex = "([a-zA-Z][a-zA-Z0-9-_]+[a-zA-Z0-9])".toRegex()

@Service
class ProfileValidationService(
    private val profileRepository: ProfileRepository
) {
    fun validateProfileInformation(
        updateAccountId: Long,
        profileInformationUpdate: GeneralProfileInformationUpdateDto,
        profilePicture: MultipartFile?,
        isProfileNew: Boolean
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (isProfileNew && profilePicture == null) {
            validationErrorBuilder.addError("profilePicture", "Profile Picture is required")
        }

        if (profileInformationUpdate.alias.isNullOrBlank()) {
            validationErrorBuilder.addError("alias", "Alias must not be blank")
        } else if (profileInformationUpdate.alias.length > ALIAS_MAX_LENGTH) {
            validationErrorBuilder.addError("alias", "Alias must not exceed $ALIAS_MAX_LENGTH characters")
        } else if (!aliasRegex.matches(profileInformationUpdate.alias)) {
            validationErrorBuilder.addError("alias", "Alias must only start with a letter and only contain letters, digits, - and _")
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
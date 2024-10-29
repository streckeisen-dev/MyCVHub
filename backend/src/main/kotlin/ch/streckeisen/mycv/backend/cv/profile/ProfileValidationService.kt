package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

val aliasRegex = "([a-zA-Z][a-zA-Z0-9-_]+[a-zA-Z0-9])".toRegex()

private const val PROFILE_VALIDATION_PREFIX_KEY = "${MYCV_KEY_PREFIX}.profile.validation"
private const val ALIAS_REGEX_ERROR_KEY = "${PROFILE_VALIDATION_PREFIX_KEY}.aliasWithInvalidChars"
private const val ALIAS_TAKEN_ERROR_KEY = "${PROFILE_VALIDATION_PREFIX_KEY}.aliasAlreadyTaken"
private const val PROFILE_PICTURE_FIELD_KEY = "profilePicture"
private const val ALIAS_FIELD_KEY = "alias"
private const val JOB_TITLE_FIELD_KEY = "jobTitle"

@Service
class ProfileValidationService(
    private val profileRepository: ProfileRepository,
    private val messagesService: MessagesService
) {
    fun validateProfileInformation(
        updateAccountId: Long,
        profileInformationUpdate: GeneralProfileInformationUpdateDto,
        profilePicture: MultipartFile?,
        isProfileNew: Boolean
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (isProfileNew && profilePicture == null) {
            val error = messagesService.requiredFieldMissingError(PROFILE_PICTURE_FIELD_KEY)
            validationErrorBuilder.addError(PROFILE_PICTURE_FIELD_KEY, error)
        }

        if (profileInformationUpdate.alias.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(ALIAS_FIELD_KEY)
            validationErrorBuilder.addError(ALIAS_FIELD_KEY, error)
        } else if (profileInformationUpdate.alias.length > ALIAS_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(ALIAS_FIELD_KEY, ALIAS_MAX_LENGTH)
            validationErrorBuilder.addError(ALIAS_FIELD_KEY, error)
        } else if (!aliasRegex.matches(profileInformationUpdate.alias)) {
            val error = messagesService.getMessage(ALIAS_REGEX_ERROR_KEY)
            validationErrorBuilder.addError(ALIAS_FIELD_KEY, error)
        } else {
            val profileWithSameAlias = profileRepository.findByAlias(profileInformationUpdate.alias).orElse(null)
            if (profileWithSameAlias != null && profileWithSameAlias.account.id != updateAccountId) {
                val error = messagesService.getMessage(ALIAS_TAKEN_ERROR_KEY)
                validationErrorBuilder.addError(ALIAS_FIELD_KEY, error)
            }
        }

        if (profileInformationUpdate.jobTitle.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(JOB_TITLE_FIELD_KEY)
            validationErrorBuilder.addError(JOB_TITLE_FIELD_KEY, error)
        } else if (profileInformationUpdate.jobTitle.length > JOB_TITLE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(JOB_TITLE_FIELD_KEY, JOB_TITLE_MAX_LENGTH)
            validationErrorBuilder.addError(JOB_TITLE_FIELD_KEY, error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage("${PROFILE_VALIDATION_PREFIX_KEY}.error")))
        }

        return Result.success(Unit)
    }
}
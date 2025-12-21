package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

private const val PROFILE_VALIDATION_PREFIX_KEY = "${MYCV_KEY_PREFIX}.profile.validation"
private const val PROFILE_PICTURE_FIELD_KEY = "profilePicture"
private const val JOB_TITLE_FIELD_KEY = "jobTitle"

@Service
class ProfileValidationService(
    private val stringValidator: StringValidator,
    private val messagesService: MessagesService
) {
    fun validateProfileInformation(
        profileInformationUpdate: GeneralProfileInformationUpdateDto,
        profilePicture: MultipartFile?,
        isProfileNew: Boolean
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (isProfileNew && profilePicture == null) {
            val error = messagesService.requiredFieldMissingError(PROFILE_PICTURE_FIELD_KEY)
            validationErrorBuilder.addError(PROFILE_PICTURE_FIELD_KEY, error)
        }

        stringValidator.validateRequiredString(
            requiredField = JOB_TITLE_FIELD_KEY,
            value = profileInformationUpdate.jobTitle,
            maxLength = JOB_TITLE_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage("${PROFILE_VALIDATION_PREFIX_KEY}.error")))
        }

        return Result.success(Unit)
    }
}
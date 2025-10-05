package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.isValidHexColor
import org.springframework.stereotype.Service

@Service
class ProfileThemeValidationService(
    private val messagesService: MessagesService
) {
    fun validateThemeUpdate(themeUpdate: ProfileThemeUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        validateColor(themeUpdate.backgroundColor, "backgroundColor", validationErrorBuilder)
        validateColor(themeUpdate.surfaceColor, "surfaceColor", validationErrorBuilder)

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("ch.streckeisen.mycv.profile.theme.validation.error"))
        }
        return Result.success(Unit)
    }

    private fun validateColor(
        colorString: String?,
        fieldName: String,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (colorString.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(fieldName)
            validationErrorBuilder.addError(fieldName, error)
        } else {
            if (!isValidHexColor(colorString)) {
                val error =
                    messagesService.getMessage("ch.streckeisen.mycv.profile.theme.validation.invalidColor", fieldName)
                validationErrorBuilder.addError(fieldName, error)
            }
        }
    }
}
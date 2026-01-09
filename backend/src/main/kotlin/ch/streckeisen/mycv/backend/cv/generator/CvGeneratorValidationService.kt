package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.isValidHexColor
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service

private const val MISSING_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.missing"
private const val INVALID_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.invalid"
private const val UNKNOWN_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.unknown"

private const val INCOMPLETE_PROFILE_MESSAGE = "$MYCV_KEY_PREFIX.cv.incompleteProfile"

@Service
class CvGeneratorValidationService(
    private val messagesService: MessagesService
) {
    fun validateTemplateOptions(cvStyle: CVStyle, templateOptions: Map<String, String>): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        templateOptions.keys.forEach { key ->
            val option = cvStyle.options.find { it.key == key }
            if (option == null) {
                validationErrorBuilder.addError(
                    "templateOptions.[$key]",
                    messagesService.getMessage(UNKNOWN_TEMPLATE_OPTION)
                )
            } else {
                val value = templateOptions[key]
                if (value == null) {
                    validationErrorBuilder.addError(
                        "templateOptions.[$key]",
                        messagesService.getMessage(MISSING_TEMPLATE_OPTION)
                    )
                } else {
                    validateOption(option, value, validationErrorBuilder)
                }
            }
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(INVALID_TEMPLATE_OPTION)))
        }
        return Result.success(Unit)
    }

    fun verifyProfileCompleteness(profile: ProfileEntity): Result<Unit> {
        if (!profile.account.isVerified || profile.account.accountDetails == null) {
            return Result.failure(LocalizedException(INCOMPLETE_PROFILE_MESSAGE))
        }

        return Result.success(Unit)
    }

    private fun validateOption(
        option: CVStyleOption,
        value: String,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        when (option.type) {
            CVStyleOptionType.COLOR -> {
                if (!isValidHexColor(value)) {
                    validationErrorBuilder.addError(
                        "templateOptions.[${option.key}]",
                        messagesService.getMessage(INVALID_TEMPLATE_OPTION)
                    )
                }
            }

            CVStyleOptionType.STRING -> {
                if (StringUtils.isBlank(value)) {
                    validationErrorBuilder.addError(
                        "templateOptions.[${option.key}]",
                        messagesService.getMessage(MISSING_TEMPLATE_OPTION)
                    )
                }
            }
        }
    }
}
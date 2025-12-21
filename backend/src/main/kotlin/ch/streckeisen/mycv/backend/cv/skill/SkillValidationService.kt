package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import org.springframework.stereotype.Service

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.skill.validationError"
private const val NAME_FIELD_KEY = "name"
private const val TYPE_FIELD_KEY = "type"
private const val LEVEL_FIELD_KEY = "level"

@Service
class SkillValidationService(
    private val stringValidator: StringValidator,
    private val messagesService: MessagesService
) {
    fun validateSkill(skillUpdate: SkillUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            requiredField = NAME_FIELD_KEY,
            value = skillUpdate.name,
            maxLength = NAME_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        stringValidator.validateRequiredString(
            requiredField = TYPE_FIELD_KEY,
            value = skillUpdate.type,
            maxLength = TYPE_MAX_LENGTH,
            validationErrorBuilder = validationErrorBuilder
        )

        when {
            skillUpdate.level == null -> {
                val error = messagesService.requiredFieldMissingError(LEVEL_FIELD_KEY)
                validationErrorBuilder.addError(LEVEL_FIELD_KEY, error)
            }

            skillUpdate.level < LEVEL_MIN_VALUE -> {
                val error = messagesService.numberTooSmallError(LEVEL_FIELD_KEY, LEVEL_MIN_VALUE.toInt())
                validationErrorBuilder.addError(LEVEL_FIELD_KEY, error)
            }

            skillUpdate.level > LEVEL_MAX_VALUE -> {
                val error = messagesService.numberTooBigError(LEVEL_FIELD_KEY, LEVEL_MAX_VALUE.toInt())
                validationErrorBuilder.addError(LEVEL_FIELD_KEY, error)
            }
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
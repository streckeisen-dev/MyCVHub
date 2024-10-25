package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.skill.validationError"
private const val NAME_FIELD_KEY = "name"
private const val TYPE_FIELD_KEY = "type"
private const val LEVEL_FIELD_KEY = "level"

@Service
class SkillValidationService(
    private val messagesService: MessagesService
) {
    fun validateSkill(skillUpdate: SkillUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (skillUpdate.name.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(NAME_FIELD_KEY)
            validationErrorBuilder.addError(NAME_FIELD_KEY, error)
        } else if (skillUpdate.name.length > NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(NAME_FIELD_KEY, NAME_MAX_LENGTH)
            validationErrorBuilder.addError(NAME_FIELD_KEY, error)
        }

        if (skillUpdate.type.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(TYPE_FIELD_KEY)
            validationErrorBuilder.addError(TYPE_FIELD_KEY, error)
        } else if (skillUpdate.type.length > TYPE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(TYPE_FIELD_KEY, TYPE_MAX_LENGTH)
            validationErrorBuilder.addError(TYPE_FIELD_KEY, error)
        }

        if (skillUpdate.level == null) {
            val error = messagesService.requiredFieldMissingError(LEVEL_FIELD_KEY)
            validationErrorBuilder.addError(LEVEL_FIELD_KEY, error)
        } else if (skillUpdate.level < LEVEL_MIN_VALUE) {
            val error = messagesService.numberTooSmallError(LEVEL_FIELD_KEY, LEVEL_MIN_VALUE.toInt())
            validationErrorBuilder.addError(LEVEL_FIELD_KEY, error)
        } else if (skillUpdate.level > LEVEL_MAX_VALUE) {
            val error = messagesService.numberTooBigError(LEVEL_FIELD_KEY, LEVEL_MAX_VALUE.toInt())
            validationErrorBuilder.addError(LEVEL_FIELD_KEY, error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
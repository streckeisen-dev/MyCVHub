package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.skill.validationError"

@Service
class SkillValidationService(
    private val messagesService: MessagesService
) {
    fun validateSkill(skillUpdate: SkillUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (skillUpdate.name.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("name")
            validationErrorBuilder.addError("name", error)
        } else if (skillUpdate.name.length > NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("name", NAME_MAX_LENGTH)
            validationErrorBuilder.addError("name", error)
        }

        if (skillUpdate.type.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("type")
            validationErrorBuilder.addError("type", error)
        } else if (skillUpdate.type.length > TYPE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("type", TYPE_MAX_LENGTH)
            validationErrorBuilder.addError("type", error)
        }

        if (skillUpdate.level == null) {
            val error = messagesService.requiredFieldMissingError("level")
            validationErrorBuilder.addError("level", error)
        } else if (skillUpdate.level < LEVEL_MIN_VALUE) {
            val error = messagesService.numberTooSmallError("level", LEVEL_MIN_VALUE.toInt())
            validationErrorBuilder.addError("level", error)
        } else if (skillUpdate.level > LEVEL_MAX_VALUE) {
            val error = messagesService.numberTooBigError("level", LEVEL_MAX_VALUE.toInt())
            validationErrorBuilder.addError("level", error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.stereotype.Service

@Service
class SkillValidationService {
    fun validateSkill(skillUpdate: SkillUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (skillUpdate.name.isNullOrBlank()) {
            validationErrorBuilder.addError("name", "Name must not be blank")
        } else if (skillUpdate.name.length > NAME_MAX_LENGTH) {
            validationErrorBuilder.addError("name", "Name must not exceed $NAME_MAX_LENGTH characters")
        }

        if (skillUpdate.type.isNullOrBlank()) {
            validationErrorBuilder.addError("type", "Type must not be blank")
        } else if (skillUpdate.type.length > TYPE_MAX_LENGTH) {
            validationErrorBuilder.addError("type", "Type must not exceed $TYPE_MAX_LENGTH characters")
        }

        if (skillUpdate.level == null) {
            validationErrorBuilder.addError("level", "Level must not be null")
        } else if (skillUpdate.level < LEVEL_MIN_VALUE) {
            validationErrorBuilder.addError("level", "Level must be greater than 0")
        } else if (skillUpdate.level > LEVEL_MAX_VALUE) {
            validationErrorBuilder.addError("level", "Level must be less than 100")
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Skill validation failed"))
        }
        return Result.success(Unit)
    }
}
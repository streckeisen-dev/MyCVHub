package ch.streckeisen.mycv.backend.cv.project

import ch.streckeisen.mycv.backend.exceptions.ValidationException.ValidationErrorBuilder
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service
import java.time.LocalDate

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.project.validation.error"
private const val NAME_FIELD_KEY = "name"
private const val ROLE_FIELD_KEY = "role"
private const val DESCRIPTION_FIELD_KEY = "description"
private const val PROJECT_START_FIELD_KEY = "projectStart"
private const val PROJECT_END_FIELD_KEY = "projectEnd"
private const val PROJECT_LINK_KEY = "projectLink"
private const val PROJECT_LINK_URL_KEY = "url"
private const val PROJECT_LINK_TYPE_KEY = "type"

@Service
class ProjectValidationService(
    private val messagesService: MessagesService
) {
    fun validateProject(projectUpdate: ProjectUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationErrorBuilder()

        if (projectUpdate.name.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(NAME_FIELD_KEY)
            validationErrorBuilder.addError(NAME_FIELD_KEY, error)
        } else if (projectUpdate.name.length > PROJECT_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(NAME_FIELD_KEY, PROJECT_NAME_MAX_LENGTH)
            validationErrorBuilder.addError(NAME_FIELD_KEY, error)
        }

        if (projectUpdate.role.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(ROLE_FIELD_KEY)
            validationErrorBuilder.addError(ROLE_FIELD_KEY, error)
        } else if (projectUpdate.role.length > ROLE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(ROLE_FIELD_KEY, ROLE_MAX_LENGTH)
            validationErrorBuilder.addError(ROLE_FIELD_KEY, error)
        }

        if (projectUpdate.description.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(DESCRIPTION_FIELD_KEY)
            validationErrorBuilder.addError(DESCRIPTION_FIELD_KEY, error)
        }

        if (projectUpdate.projectStart == null) {
            val error = messagesService.requiredFieldMissingError(PROJECT_START_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_START_FIELD_KEY, error)
        } else if (projectUpdate.projectStart.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(PROJECT_START_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_START_FIELD_KEY, error)
        }

        if (projectUpdate.projectEnd != null && projectUpdate.projectEnd.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(PROJECT_END_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_END_FIELD_KEY, error)
        } else if (
            projectUpdate.projectEnd != null
            && projectUpdate.projectStart != null
            && projectUpdate.projectStart.isAfter(projectUpdate.projectEnd)
        ) {
            val error = messagesService.dateIsAfterError(PROJECT_START_FIELD_KEY, PROJECT_END_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_END_FIELD_KEY, error)
        }

        projectUpdate.links?.forEachIndexed { index, link ->
            if (link.url.isNullOrBlank()) {
                val error = messagesService.requiredFieldMissingError(PROJECT_LINK_URL_KEY)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$PROJECT_LINK_URL_KEY", error)
            }

            if (link.type == null) {
                val error = messagesService.requiredFieldMissingError(PROJECT_LINK_TYPE_KEY)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$PROJECT_LINK_TYPE_KEY", error)
            }
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(VALIDATION_ERROR_KEY))
        }
        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.cv.project

import ch.streckeisen.mycv.backend.exceptions.ValidationException.ValidationErrorBuilder
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.isUrlValid
import org.springframework.stereotype.Service
import java.time.LocalDate

private const val VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.project.validation.error"
private const val NAME_FIELD_KEY = "name"
private const val ROLE_FIELD_KEY = "role"
private const val DESCRIPTION_FIELD_KEY = "description"
private const val PROJECT_START_FIELD_KEY = "projectStart"
private const val PROJECT_END_FIELD_KEY = "projectEnd"
private const val PROJECT_LINK_KEY = "links"
private const val PROJECT_LINK_URL_KEY = "url"
private const val PROJECT_LINK_TYPE_KEY = "type"
private const val DISPLAY_NAME_KEY = "displayName"

@Service
class ProjectValidationService(
    private val messagesService: MessagesService
) {
    fun validateProject(projectUpdate: ProjectUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationErrorBuilder()

        validateProjectName(projectUpdate.name, validationErrorBuilder)
        validateProjectRole(projectUpdate.role, validationErrorBuilder)
        validateDescription(projectUpdate.description, validationErrorBuilder)
        validateProjectStart(projectUpdate.projectStart, validationErrorBuilder)
        validateProjectEnd(projectUpdate.projectEnd, projectUpdate.projectStart, validationErrorBuilder)
        validateProjectLinks(projectUpdate.links, validationErrorBuilder)

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(VALIDATION_ERROR_KEY))
        }
        return Result.success(Unit)
    }

    private fun validateProjectName(
        projectName: String?,
        validationErrorBuilder: ValidationErrorBuilder
    ) {
        if (projectName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(NAME_FIELD_KEY)
            validationErrorBuilder.addError(NAME_FIELD_KEY, error)
        } else if (projectName.length > PROJECT_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(NAME_FIELD_KEY, PROJECT_NAME_MAX_LENGTH)
            validationErrorBuilder.addError(NAME_FIELD_KEY, error)
        }
    }

    private fun validateProjectRole(
        projectRole: String?,
        validationErrorBuilder: ValidationErrorBuilder
    ) {
        if (projectRole.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(ROLE_FIELD_KEY)
            validationErrorBuilder.addError(ROLE_FIELD_KEY, error)
        } else if (projectRole.length > ROLE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(ROLE_FIELD_KEY, ROLE_MAX_LENGTH)
            validationErrorBuilder.addError(ROLE_FIELD_KEY, error)
        }
    }

    private fun validateDescription(
        description: String?,
        validationErrorBuilder: ValidationErrorBuilder
    ) {
        if (description.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(DESCRIPTION_FIELD_KEY)
            validationErrorBuilder.addError(DESCRIPTION_FIELD_KEY, error)
        }
    }

    private fun validateProjectStart(
        projectStart: LocalDate?,
        validationErrorBuilder: ValidationErrorBuilder
    ) {
        if (projectStart == null) {
            val error = messagesService.requiredFieldMissingError(PROJECT_START_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_START_FIELD_KEY, error)
        } else if (projectStart.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(PROJECT_START_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_START_FIELD_KEY, error)
        }
    }

    private fun validateProjectEnd(
        projectEnd: LocalDate?,
        projectStart: LocalDate?,
        validationErrorBuilder: ValidationErrorBuilder
    ) {
        if (projectEnd != null && projectEnd.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(PROJECT_END_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_END_FIELD_KEY, error)
        } else if (
            projectEnd != null
            && projectStart != null
            && projectStart.isAfter(projectEnd)
        ) {
            val error = messagesService.dateIsAfterError(PROJECT_START_FIELD_KEY, PROJECT_END_FIELD_KEY)
            validationErrorBuilder.addError(PROJECT_END_FIELD_KEY, error)
        }
    }

    private fun validateProjectLinks(
        projectLinks: List<ProjectLinkUpdateDto>?,
        validationErrorBuilder: ValidationErrorBuilder
    ) {
        projectLinks?.forEachIndexed { index, link ->
            if (link.url.isNullOrBlank()) {
                val error = messagesService.requiredFieldMissingError(PROJECT_LINK_URL_KEY)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$PROJECT_LINK_URL_KEY", error)
            } else if (!isUrlValid(link.url)) {
                val error = messagesService.invalidUrlError(PROJECT_LINK_URL_KEY)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$PROJECT_LINK_URL_KEY", error)
            }

            if (link.displayName.isNullOrBlank()) {
                val error = messagesService.requiredFieldMissingError(DISPLAY_NAME_KEY)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$DISPLAY_NAME_KEY", error)
            } else if (link.displayName.length > PROJECT_LINK_NAME_MAX_LENGTH) {
                val error = messagesService.fieldMaxLengthExceededError(DISPLAY_NAME_KEY, PROJECT_LINK_NAME_MAX_LENGTH)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$DISPLAY_NAME_KEY", error)
            }

            if (link.type == null) {
                val error = messagesService.requiredFieldMissingError(PROJECT_LINK_TYPE_KEY)
                validationErrorBuilder.addError("$PROJECT_LINK_KEY[$index].$PROJECT_LINK_TYPE_KEY", error)
            }
        }
    }
}
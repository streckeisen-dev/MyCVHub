package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.cv.generator.CVStyle
import ch.streckeisen.mycv.backend.cv.generator.CvGeneratorValidationService
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import org.springframework.stereotype.Service

private const val NAME_FIELD_KEY = "name"
private const val CV_TEMPLATE_FIELD = "cvTemplate"
private const val CV_CONFIG_FILED = "cvConfiguration"
private const val PROFILE_FIELD = "profile"
private const val INCLUDED_EXPERIENCE_FIELD = "includedWorkExperience"
private const val INCLUDED_EDUCATION_FIELD = "includedEducation"
private const val INCLUDED_PROJECTS_FIELD = "includedProjects"
private const val INCLUDED_SKILLS_FIELD = "includedSkills"

private const val APPLICATION_TEMPLATE_MESSAGE_PREFIX = "$MYCV_KEY_PREFIX.applicationTemplate.validation"
private const val CV_CONFIG_MISSING_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.cvConfigurationMissing"
private const val INVALID_CV_STYLE_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.invalidCvStyle"
private const val INVALID_TEMPLATE_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.invalidTemplate"
private const val UNKNOWN_CV_ENTRY_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.unknownCvEntries"

@Service
class ApplicationTemplateValidationService(
    private val stringValidator: StringValidator,
    private val cvGeneratorValidationService: CvGeneratorValidationService,
    private val messagesService: MessagesService
) {
    fun validateUpdate(applicationTemplateUpdate: ApplicationTemplateUpdateDto, profile: ProfileEntity): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            NAME_FIELD_KEY,
            applicationTemplateUpdate.name,
            NAME_MAX_LENGTH,
            validationErrorBuilder
        )

        if (applicationTemplateUpdate.cvConfiguration == null) {
            val error = messagesService.getMessage(CV_CONFIG_MISSING_MESSAGE_KEY)
            validationErrorBuilder.addError(CV_CONFIG_FILED, error)
        } else {
            validateCvConfiguration(applicationTemplateUpdate.cvConfiguration, profile, validationErrorBuilder)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(INVALID_TEMPLATE_MESSAGE_KEY)))
        }
        return Result.success(Unit)
    }

    fun validateCvConfiguration(
        cvConfiguration: CvConfigurationUpdateDto,
        profile: ProfileEntity,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        val cvTemplate =
            if (cvConfiguration.cvTemplate != null) CVStyle.fromTemplate(cvConfiguration.cvTemplate) else null
        if (cvConfiguration.cvTemplate == null) {
            val error = messagesService.requiredFieldMissingError(CV_TEMPLATE_FIELD)
            validationErrorBuilder.addError(CV_TEMPLATE_FIELD, error)
        } else if (cvTemplate == null) {
            val error = messagesService.getMessage(INVALID_CV_STYLE_MESSAGE_KEY)
            validationErrorBuilder.addError(CV_TEMPLATE_FIELD, error)
        }

        val profileValidation = cvGeneratorValidationService.verifyProfileCompleteness(profile)
        if (profileValidation.isFailure) {
            validationErrorBuilder.addError(
                PROFILE_FIELD,
                messagesService.getMessage(profileValidation.exceptionOrNull()!!.message!!)
            )
        } else {
            val unknownWorkExperience = cvConfiguration.includedWorkExperience?.filter { includedExperience ->
                profile.workExperiences.none { includedExperience.entityId == it.id }
            }
            val unknownEducation = cvConfiguration.includedEducation?.filter { includedEducation ->
                profile.education.none { includedEducation.entityId == it.id }
            }
            val unknownProjects = cvConfiguration.includedWorkExperience?.filter { includedProject ->
                profile.projects.none { includedProject.entityId == it.id }
            }
            val unknownSkills = cvConfiguration.includedSkills?.filter { includedSkillId ->
                profile.skills.none { it.id == includedSkillId }
            }

            if (!unknownWorkExperience.isNullOrEmpty()) {
                validationErrorBuilder.addError(
                    INCLUDED_EXPERIENCE_FIELD,
                    messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                )
            }

            if (!unknownEducation.isNullOrEmpty()) {
                validationErrorBuilder.addError(
                    INCLUDED_EDUCATION_FIELD,
                    messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                )
            }

            if (!unknownProjects.isNullOrEmpty()) {
                validationErrorBuilder.addError(
                    INCLUDED_PROJECTS_FIELD,
                    messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                )
            }

            if (!unknownSkills.isNullOrEmpty()) {
                validationErrorBuilder.addError(
                    INCLUDED_SKILLS_FIELD,
                    messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                )
            }
        }

        if (cvTemplate != null && cvConfiguration.templateOptions != null) {
            cvGeneratorValidationService.validateTemplateOptions(cvTemplate, cvConfiguration.templateOptions)
                .onFailure { ex ->
                    val errors = (ex as ValidationException).errors
                    errors.forEach { (field, message) -> validationErrorBuilder.addError(field, message) }
                }
        }
    }
}
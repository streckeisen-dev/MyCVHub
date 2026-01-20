package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.applicationTemplate.dto.ApplicationTemplateUpdateDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CoverLetterConfigurationUpdateDto
import ch.streckeisen.mycv.backend.coverletter.CoverLetterGenerationValidationService
import ch.streckeisen.mycv.backend.cv.generator.CVStyle
import ch.streckeisen.mycv.backend.cv.generator.CvConfigurationRequestDto
import ch.streckeisen.mycv.backend.cv.generator.CvGeneratorValidationService
import ch.streckeisen.mycv.backend.cv.generator.IncludedCvContentDto
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import io.micrometer.observation.aop.ObservationKeyValueAnnotationHandler
import org.springframework.stereotype.Service

private const val NAME_FIELD_KEY = "name"
private const val CV_STYLE_FIELD = "cvStyle"
private const val CV_CONFIG_FILED = "cvConfiguration"
private const val PROFILE_FIELD = "profile"
private const val INCLUDED_EXPERIENCE_FIELD = "includedWorkExperience"
private const val INCLUDED_EDUCATION_FIELD = "includedEducation"
private const val INCLUDED_PROJECTS_FIELD = "includedProjects"
private const val INCLUDED_SKILLS_FIELD = "includedSkills"
private const val COVER_LETTER_CONFIG_FIELD = "coverLetterConfiguration"

private const val APPLICATION_TEMPLATE_MESSAGE_PREFIX = "$MYCV_KEY_PREFIX.applicationTemplate.validation"
private const val CV_CONFIG_MISSING_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.cvConfigurationMissing"
private const val INVALID_CV_STYLE_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.invalidCvStyle"
private const val INVALID_TEMPLATE_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.invalidTemplate"
private const val UNKNOWN_CV_ENTRY_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.unknownCvEntries"
private const val NAME_TAKEN_MESSAGE_KEY = "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.nameTaken"
private const val COVER_LETTER_CONFIG_MISSING_MESSAGE_KEY =
    "$APPLICATION_TEMPLATE_MESSAGE_PREFIX.coverLetterConfigurationMissing"


@Service
class ApplicationTemplateValidationService(
    private val stringValidator: StringValidator,
    private val cvGeneratorValidationService: CvGeneratorValidationService,
    private val coverLetterGenerationValidationService: CoverLetterGenerationValidationService,
    private val messagesService: MessagesService,
    private val applicationTemplateRepository: ApplicationTemplateRepository
) {
    fun validateUpdate(
        accountId: Long,
        applicationTemplateUpdate: ApplicationTemplateUpdateDto,
        profile: ProfileEntity
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(
            NAME_FIELD_KEY,
            applicationTemplateUpdate.name,
            NAME_MAX_LENGTH,
            validationErrorBuilder
        )

        if (applicationTemplateUpdate.name != null) {
            val existingTemplate =
                applicationTemplateRepository.findByAccountIdAndName(accountId, applicationTemplateUpdate.name)
            if (existingTemplate.isPresent && existingTemplate.get().id != applicationTemplateUpdate.id) {
                validationErrorBuilder.addError(NAME_FIELD_KEY, messagesService.getMessage(NAME_TAKEN_MESSAGE_KEY))
            }
        }

        if (applicationTemplateUpdate.cvConfiguration == null) {
            val error = messagesService.getMessage(CV_CONFIG_MISSING_MESSAGE_KEY)
            validationErrorBuilder.addError(CV_CONFIG_FILED, error)
        } else {
            validateCvConfiguration(applicationTemplateUpdate.cvConfiguration, profile, validationErrorBuilder)
        }

        if (applicationTemplateUpdate.coverLetterConfiguration == null) {
            val error = messagesService.getMessage(COVER_LETTER_CONFIG_MISSING_MESSAGE_KEY)
            validationErrorBuilder.addError(COVER_LETTER_CONFIG_FIELD, error)
        } else {
            validateCoverLetterConfiguration(
                applicationTemplateUpdate.coverLetterConfiguration,
                validationErrorBuilder
            )
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(INVALID_TEMPLATE_MESSAGE_KEY)))
        }
        return Result.success(Unit)
    }

    fun validateCvConfiguration(
        cvConfiguration: CvConfigurationRequestDto,
        profile: ProfileEntity,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        val internalErrorBuilder = ValidationException.ValidationErrorBuilder()
        val cvStyle =
            if (cvConfiguration.cvStyle != null) CVStyle.fromStyleKey(cvConfiguration.cvStyle) else null
        if (cvConfiguration.cvStyle == null) {
            val error = messagesService.requiredFieldMissingError(CV_STYLE_FIELD)
            internalErrorBuilder.addError(CV_STYLE_FIELD, error)
        } else if (cvStyle == null) {
            val error = messagesService.getMessage(INVALID_CV_STYLE_MESSAGE_KEY)
            internalErrorBuilder.addError(CV_STYLE_FIELD, error)
        }

        val profileValidation = cvGeneratorValidationService.validateProfileCompleteness(profile)
        if (profileValidation.isFailure) {
            internalErrorBuilder.addError(
                PROFILE_FIELD,
                messagesService.getMessage(profileValidation.exceptionOrNull()!!.message!!)
            )
        } else {
            if (cvConfiguration.includedCvContent != null) {
                validateCvContentConfig(cvConfiguration.includedCvContent, profile, validationErrorBuilder)
            }
        }

        if (cvStyle != null && cvConfiguration.cvStyleOptions != null) {
            cvGeneratorValidationService.validateStyleOptions(
                cvStyle,
                cvConfiguration.cvStyleOptions,
                internalErrorBuilder
            )
        }

        internalErrorBuilder.errors()
            .forEach { (key, value) -> validationErrorBuilder.addError("$CV_CONFIG_FILED.$key", value) }
    }

    private fun validateCvContentConfig(
        cvContent: IncludedCvContentDto,
        profile: ProfileEntity,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        val unknownWorkExperience =
            cvContent.includedWorkExperience?.filter { includedExperience ->
                profile.workExperiences.none { includedExperience.id == it.id }
            }
        val unknownEducation =
            cvContent.includedEducation?.filter { includedEducation ->
                profile.education.none { includedEducation.id == it.id }
            }
        val unknownProjects = cvContent.includedProjects?.filter { includedProject ->
            profile.projects.none { includedProject.id == it.id }
        }
        val unknownSkills = cvContent.includedSkills?.filter { includedSkillId ->
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

    fun validateCoverLetterConfiguration(
        coverLetterConfig: CoverLetterConfigurationUpdateDto,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        val internalErrorBuilder = ValidationException.ValidationErrorBuilder()
        coverLetterGenerationValidationService.validateCoverLetterStyle(coverLetterConfig.style, internalErrorBuilder)

        coverLetterGenerationValidationService.validateLanguage(coverLetterConfig.language, internalErrorBuilder)

        coverLetterGenerationValidationService.validateContent(coverLetterConfig.content, internalErrorBuilder)

        coverLetterGenerationValidationService.validateClosing(coverLetterConfig.closing, internalErrorBuilder)

        coverLetterGenerationValidationService.validateDocuments(coverLetterConfig.documents, internalErrorBuilder)

        internalErrorBuilder.errors()
            .forEach { (key, value) -> validationErrorBuilder.addError("$COVER_LETTER_CONFIG_FIELD.$key", value) }
    }
}
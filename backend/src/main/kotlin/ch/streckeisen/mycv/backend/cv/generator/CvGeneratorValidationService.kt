package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.isValidHexColor
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service

private const val CV_STYLE_FIELD = "cvStyle"
private const val INCLUDED_CONTENT_FIELD = "includedCvContent"
private const val INCLUDED_EXPERIENCE_FIELD = "includedWorkExperience"
private const val INCLUDED_EDUCATION_FIELD = "includedEducation"
private const val INCLUDED_PROJECTS_FIELD = "includedProjects"
private const val INCLUDED_SKILLS_FIELD = "includedSkills"

private const val CV_MESSAGE_PREFIX = "$MYCV_KEY_PREFIX.cv.validation"

private const val MISSING_TEMPLATE_OPTION = "$CV_MESSAGE_PREFIX.templateOptions.missing"
private const val INVALID_TEMPLATE_OPTION = "$CV_MESSAGE_PREFIX.templateOptions.invalid"
private const val UNKNOWN_TEMPLATE_OPTION = "$CV_MESSAGE_PREFIX.templateOptions.unknown"

private const val INVALID_CONFIG_MESSAGE = "$CV_MESSAGE_PREFIX.invalidConfig"
private const val INVALID_STYLE_MESSAGE = "$CV_MESSAGE_PREFIX.invalidStyle"
private const val INCOMPLETE_PROFILE_MESSAGE = "$CV_MESSAGE_PREFIX.incompleteProfile"
private const val INCOMPLETE_CONTENT_CONFIG_MESSAGE = "$CV_MESSAGE_PREFIX.incompleteContentConfig"
private const val UNKNOWN_CV_ENTRY_MESSAGE_KEY = "$CV_MESSAGE_PREFIX.unknownCvEntries"
private const val NO_CV_ENTRIES_MESSAGE = "$CV_MESSAGE_PREFIX.noCvEntries"

@Service
class CvGeneratorValidationService(
    private val messagesService: MessagesService,
    private val stringValidator: StringValidator
) {
    fun validateConfiguration(cvConfiguration: CvConfigurationRequestDto, profile: ProfileEntity): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        stringValidator.validateRequiredString(CV_STYLE_FIELD, cvConfiguration.cvStyle, validationErrorBuilder)
        var cvStyle: CVStyle? = null
        if (cvConfiguration.cvStyle != null) {
            cvStyle = CVStyle.fromStyleKey(cvConfiguration.cvStyle)
            if (cvStyle == null) {
                validationErrorBuilder.addError(CV_STYLE_FIELD, messagesService.getMessage(INVALID_STYLE_MESSAGE))
            }
        }

        if (cvStyle != null && cvConfiguration.cvStyleOptions != null) {
            validateStyleOptions(cvStyle, cvConfiguration.cvStyleOptions, validationErrorBuilder)
        }

        if (cvConfiguration.includedCvContent != null) {
            if (
                cvConfiguration.includedCvContent.includedWorkExperience == null
                || cvConfiguration.includedCvContent.includedEducation == null
                || cvConfiguration.includedCvContent.includedProjects == null
                || cvConfiguration.includedCvContent.includedSkills == null
            ) {
                validationErrorBuilder.addError(
                    INCLUDED_CONTENT_FIELD,
                    messagesService.getMessage(INCOMPLETE_CONTENT_CONFIG_MESSAGE)
                )
            } else {
                if (
                    cvConfiguration.includedCvContent.includedWorkExperience.isEmpty()
                    && cvConfiguration.includedCvContent.includedEducation.isEmpty()
                    && cvConfiguration.includedCvContent.includedProjects.isEmpty()
                    && cvConfiguration.includedCvContent.includedSkills.isEmpty()
                ) {
                    validationErrorBuilder.addError(
                        INCLUDED_CONTENT_FIELD,
                        messagesService.getMessage(NO_CV_ENTRIES_MESSAGE)
                    )
                } else {
                    val unknownWorkExperience =
                        cvConfiguration.includedCvContent.includedWorkExperience.filter { includedExperience ->
                            profile.workExperiences.none { includedExperience.id == it.id }
                        }
                    val unknownEducation =
                        cvConfiguration.includedCvContent.includedEducation.filter { includedEducation ->
                            profile.education.none { includedEducation.id == it.id }
                        }
                    val unknownProjects = cvConfiguration.includedCvContent.includedProjects.filter { includedProject ->
                        profile.projects.none { includedProject.id == it.id }
                    }
                    val unknownSkills = cvConfiguration.includedCvContent.includedSkills.filter { includedSkillId ->
                        profile.skills.none { it.id == includedSkillId }
                    }

                    if (unknownWorkExperience.isNotEmpty()) {
                        validationErrorBuilder.addError(
                            "$INCLUDED_CONTENT_FIELD.$INCLUDED_EXPERIENCE_FIELD",
                            messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                        )
                    }

                    if (unknownEducation.isNotEmpty()) {
                        validationErrorBuilder.addError(
                            "$INCLUDED_CONTENT_FIELD.$INCLUDED_EDUCATION_FIELD",
                            messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                        )
                    }

                    if (unknownProjects.isNotEmpty()) {
                        validationErrorBuilder.addError(
                            "$INCLUDED_CONTENT_FIELD.$INCLUDED_PROJECTS_FIELD",
                            messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                        )
                    }

                    if (unknownSkills.isNotEmpty()) {
                        validationErrorBuilder.addError(
                            "$INCLUDED_CONTENT_FIELD.$INCLUDED_SKILLS_FIELD",
                            messagesService.getMessage(UNKNOWN_CV_ENTRY_MESSAGE_KEY)
                        )
                    }
                }
            }
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(INVALID_CONFIG_MESSAGE)))
        }

        return Result.success(Unit)
    }

    fun validateStyleOptions(
        cvStyle: CVStyle,
        templateOptions: Map<String, String>,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
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
    }

    fun validateProfileCompleteness(profile: ProfileEntity): Result<Unit> {
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
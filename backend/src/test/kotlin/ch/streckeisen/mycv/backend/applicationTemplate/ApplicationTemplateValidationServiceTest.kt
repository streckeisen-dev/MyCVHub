package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.cv.generator.CvGeneratorValidationService
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.assertValidationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

private val profile = ProfileEntity(
    id = 1L,
    jobTitle = "Job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = false,
    isPhonePublic = false,
    isAddressPublic = false,
    hideDescriptions = false,
    profilePicture = "picture.png",
    workExperiences = listOf(
        mockk {
            every { id } returns 1L
        }
    ),
    education = listOf(
        mockk {
            every { id } returns 2L
        }
    ),
    projects = listOf(
        mockk {
            every { id } returns 3L
        }
    ),
    skills = listOf(
        mockk {
            every { id } returns 4L
        }
    ),
    account = mockk()
)

private val invalidProfile: ProfileEntity = mockk()

class ApplicationTemplateValidationServiceTest {
    private lateinit var cvGeneratorValidationService: CvGeneratorValidationService
    private lateinit var applicationTemplateRepository: ApplicationTemplateRepository
    private lateinit var applicationTemplateValidationService: ApplicationTemplateValidationService

    @BeforeEach
    fun setup() {
        cvGeneratorValidationService = mockk {
            every { validateProfileCompleteness(match { it == profile }) } returns Result.success(Unit)
            every { validateProfileCompleteness(match { it == invalidProfile }) } returns Result.failure(
                IllegalArgumentException("")
            )
        }

        applicationTemplateRepository = mockk {
            every { findByAccountIdAndName(any(), any()) } returns Optional.empty()
            every { findByAccountIdAndName(eq(1), eq("taken")) } returns Optional.of(mockk {
                every { id } returns 5
            })
        }

        val messagesService: MessagesService = mockk(relaxed = true)
        applicationTemplateValidationService =
            ApplicationTemplateValidationService(
                StringValidator(messagesService),
                cvGeneratorValidationService,
                messagesService,
                applicationTemplateRepository
            )
    }

    @Test
    fun testValidateApplicationTemplateWithNullValues() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(null, null, null, null),
            profile
        )

        assertValidationResult(result, false, 2)
    }

    @Test
    fun testValidateApplicationTemplateWithTooLongName() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                null,
                "n".repeat(NAME_MAX_LENGTH + 1),
                null,
                null
            ),
            profile
        )

        assertValidationResult(result, false, 2)
    }

    @Test
    fun testValidateApplicationTemplateWithNameAlreadyTaken() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                null,
                "taken",
                null,
                null
            ),
            profile
        )

        assertValidationResult(result, false, 2)
    }

    @Test
    fun testValidateApplicationTemplateWithNameTakenBySameEntity() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                5,
                "taken",
                null,
                null
            ),
            profile
        )

        assertValidationResult(result, false, 1)
    }

    @Test
    fun testValidateApplicationTemplateWithValidName() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                null,
                "name",
                null,
                null
            ),
            profile
        )

        assertValidationResult(result, false, 1)
    }

    @Test
    fun testValidateApplicationTemplateWithInvalidCvConfig() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                id = null,
                name = "name",
                cvConfiguration = CvConfigurationUpdateDto(
                    includedWorkExperience = null,
                    includedEducation = null,
                    includedProjects = null,
                    includedSkills = null,
                    cvTemplate = null,
                    templateOptions = null
                ),
                documentChecklist = null
            ),
            profile
        )

        assertValidationResult(result, false, 1)
    }

    @Test
    fun testValidateApplicationTemplateWithValidCvConfig() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                id = null,
                name = "name",
                cvConfiguration = CvConfigurationUpdateDto(
                    includedWorkExperience = null,
                    includedEducation = null,
                    includedProjects = null,
                    includedSkills = null,
                    cvTemplate = "talendo",
                    templateOptions = null
                ),
                documentChecklist = null
            ),
            profile
        )

        assertValidationResult(result, true, 0)
    }

    @Test
    fun testValidateCvConfigurationWithInvalidProfile() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            cvConfiguration = CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = null,
                includedSkills = null,
                cvTemplate = null,
                templateOptions = null
            ),
            profile = invalidProfile,
            validationErrorBuilder = validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(2, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithInvalidTemplate() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            cvConfiguration = CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "invalid",
                templateOptions = null
            ),
            profile = profile,
            validationErrorBuilder = validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithInvalidExperienceFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            cvConfiguration = CvConfigurationUpdateDto(
                includedWorkExperience = listOf(CvEntrySelectionUpdateDto(2, null)),
                includedEducation = null,
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile = profile,
            validationErrorBuilder = validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithValidExperienceFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            cvConfiguration = CvConfigurationUpdateDto(
                includedWorkExperience = listOf(CvEntrySelectionUpdateDto(1, null)),
                includedEducation = null,
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile = profile,
            validationErrorBuilder = validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCvConfigurationWithInvalidEducationFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = listOf(CvEntrySelectionUpdateDto(1, null)),
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithValidEducationFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = listOf(CvEntrySelectionUpdateDto(2, null)),
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCvConfigurationWithInvalidProjectFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = listOf(CvEntrySelectionUpdateDto(1, null)),
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithValidProjectFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = listOf(CvEntrySelectionUpdateDto(3, null)),
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCvConfigurationWithInvalidSkillFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = null,
                includedSkills = listOf(1),
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithValidSkillFilter() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = null,
                includedSkills = listOf(4),
                cvTemplate = "talendo",
                templateOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCvConfigurationWithInvalidTemplateOptions() {
        every { cvGeneratorValidationService.validateTemplateOptions(any(), any()) } returns Result.failure(
            ValidationException.ValidationErrorBuilder()
                .addError("test", "error")
                .build("")
        )
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = mapOf("invalid" to "invalid")
            ),
            profile,
            validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithValidTemplateOptions() {
        every { cvGeneratorValidationService.validateTemplateOptions(any(), any()) } returns Result.success(Unit)
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationUpdateDto(
                includedWorkExperience = null,
                includedEducation = null,
                includedProjects = null,
                includedSkills = null,
                cvTemplate = "talendo",
                templateOptions = mapOf("valid" to "valid")
            ),
            profile,
            validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }
}
package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.applicationTemplate.dto.ApplicationTemplateUpdateDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CoverLetterConfigurationUpdateDto
import ch.streckeisen.mycv.backend.coverletter.CoverLetterGenerationValidationService
import ch.streckeisen.mycv.backend.coverletter.CoverLetterStyle
import ch.streckeisen.mycv.backend.cv.generator.CVEducationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVGenerationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVProjectSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVSkillSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVWorkExperienceSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CvConfigurationRequestDto
import ch.streckeisen.mycv.backend.cv.generator.CvGeneratorValidationService
import ch.streckeisen.mycv.backend.cv.generator.IncludedCVItem
import ch.streckeisen.mycv.backend.cv.generator.IncludedCvContentDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import ch.streckeisen.mycv.backend.util.assertValidationResult
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional

private val profile = CVGenerationSnapshot(
    accountId = 1,
    isVerified = true,
    firstName = "Test",
    lastName = "User",
    email = "test@example.test",
    phone = "+41 79 123 45 67",
    street = "Street",
    houseNumber = null,
    postcode = "1234",
    city = "City",
    birthday = LocalDate.of(1990, 1, 1),
    language = "en",
    profilePicture = "picture.png",
    jobTitle = "Job",
    bio = null,
    workExperiences = listOf(
        CVWorkExperienceSnapshot(
            id = 1,
            jobTitle = "Job",
            company = "Company",
            positionStart = LocalDate.of(2020, 1, 1),
            positionEnd = null,
            location = "Location",
            description = "Description"
        )
    ),
    education = listOf(
        CVEducationSnapshot(
            id = 2,
            institution = "Institution",
            location = "Location",
            educationStart = LocalDate.of(2015, 1, 1),
            educationEnd = null,
            degreeName = "Degree",
            description = "Description"
        )
    ),
    projects = listOf(
        CVProjectSnapshot(
            id = 3,
            name = "Project",
            role = "Role",
            description = "Description",
            projectStart = LocalDate.of(2022, 1, 1),
            projectEnd = null,
            links = emptyList()
        )
    ),
    skills = listOf(CVSkillSnapshot(id = 4, name = "Skill", type = "type", level = 5))
)

private val invalidProfile = profile.copy(isVerified = false)

class ApplicationTemplateValidationServiceTest {
    private lateinit var cvGeneratorValidationService: CvGeneratorValidationService
    private lateinit var coverLetterGenerationValidationService: CoverLetterGenerationValidationService
    private lateinit var applicationTemplateRepository: ApplicationTemplateRepository
    private lateinit var applicationTemplateValidationService: ApplicationTemplateValidationService

    @BeforeEach
    fun setup() {
        cvGeneratorValidationService = mockk {
            every { validateProfileCompleteness(eq(profile)) } returns Result.success(Unit)
            every { validateProfileCompleteness(eq(invalidProfile)) } returns Result.failure(
                IllegalArgumentException("")
            )
        }

        coverLetterGenerationValidationService = mockk {
            every { validateLanguage(any(), any())} answers {
                secondArg<ValidationException.ValidationErrorBuilder>().addError("invalid", "invalid")
            }
            every { validateLanguage(eq("en"), any()) } just Runs
            every { validateCoverLetterStyle(any(), any())} answers {
                secondArg<ValidationException.ValidationErrorBuilder>().addError("invalid", "invalid")
            }
            every { validateCoverLetterStyle(eq(CoverLetterStyle.MODERN.styleKey), any()) } just Runs
            every { validateContent(any(), any())} answers {
                secondArg<ValidationException.ValidationErrorBuilder>().addError("invalid", "invalid")
            }
            every { validateContent(eq("c"), any()) } just Runs
            every { validateClosing(any(), any())} answers {
                secondArg<ValidationException.ValidationErrorBuilder>().addError("invalid", "invalid")
            }
            every { validateClosing(eq("d"), any()) } just Runs
            every { validateDocuments(any(), any())} answers {
                secondArg<ValidationException.ValidationErrorBuilder>().addError("invalid", "invalid")
            }
            every { validateDocuments(isNull(), any()) } just Runs
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
                coverLetterGenerationValidationService,
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

        assertValidationResult(result, false, 3)
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

        assertValidationResult(result, false, 3)
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

        assertValidationResult(result, false, 3)
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

        assertValidationResult(result, false, 2)
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

        assertValidationResult(result, false, 2)
    }

    @Test
    fun testValidateApplicationTemplateWithInvalidCvConfig() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                id = null,
                name = "name",
                cvConfiguration = CvConfigurationRequestDto(
                    includedCvContent = null,
                    cvStyle = null,
                    cvStyleOptions = null
                ),
                coverLetterConfiguration = null
            ),
            profile
        )

        assertValidationResult(result, false, 2)
    }

    @Test
    fun testValidateApplicationTemplateWithValidConfig() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                id = null,
                name = "name",
                cvConfiguration = CvConfigurationRequestDto(
                    includedCvContent = null,
                    cvStyle = "talendo",
                    cvStyleOptions = null
                ),
                coverLetterConfiguration = CoverLetterConfigurationUpdateDto(
                    style = CoverLetterStyle.MODERN.styleKey,
                    language = "en",
                    mirrorProfileImage = false,
                    content = "c",
                    closing = "d",
                    documents = null
                )
            ),
            profile
        )

        assertValidationResult(result, true, 0)
    }

    @Test
    fun testValidateCvConfigurationWithInvalidProfile() {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            cvConfiguration = CvConfigurationRequestDto(
                includedCvContent = null,
                cvStyle = null,
                cvStyleOptions = null
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
            cvConfiguration = CvConfigurationRequestDto(
                includedCvContent = null,
                cvStyle = "invalid",
                cvStyleOptions = null
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
            cvConfiguration = CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = listOf(IncludedCVItem(2, null)),
                    includedEducation = null,
                    includedProjects = null,
                    includedSkills = null,
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            cvConfiguration = CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = listOf(IncludedCVItem(1, null)),
                    includedEducation = null,
                    includedProjects = null,
                    includedSkills = null,
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = null,
                    includedEducation = listOf(IncludedCVItem(1, null)),
                    includedProjects = null,
                    includedSkills = null,
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = null,
                    includedEducation = listOf(IncludedCVItem(2, null)),
                    includedProjects = null,
                    includedSkills = null,
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = null,
                    includedEducation = null,
                    includedProjects = listOf(IncludedCVItem(1, null)),
                    includedSkills = null,
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = null,
                    includedEducation = null,
                    includedProjects = listOf(IncludedCVItem(3, null)),
                    includedSkills = null,
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = null,
                    includedEducation = null,
                    includedProjects = null,
                    includedSkills = listOf(1),
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
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
            CvConfigurationRequestDto(
                includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = null,
                    includedEducation = null,
                    includedProjects = null,
                    includedSkills = listOf(4)
                ),
                cvStyle = "talendo",
                cvStyleOptions = null
            ),
            profile,
            validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateCvConfigurationWithInvalidTemplateOptions() {
        every { cvGeneratorValidationService.validateStyleOptions(any(), any(), any()) } answers {
            thirdArg<ValidationException.ValidationErrorBuilder>().addError("invalid", "invalid")
        }
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationRequestDto(
                includedCvContent = null,
                cvStyle = "talendo",
                cvStyleOptions = mapOf("invalid" to "invalid")
            ),
            profile,
            validationErrorBuilder
        )

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateCvConfigurationWithValidTemplateOptions() {
        every { cvGeneratorValidationService.validateStyleOptions(any(), any(), any()) } just Runs
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicationTemplateValidationService.validateCvConfiguration(
            CvConfigurationRequestDto(
                includedCvContent = null,
                cvStyle = "talendo",
                cvStyleOptions = mapOf("valid" to "valid")
            ),
            profile,
            validationErrorBuilder
        )

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateUpdateWithInvalidCoverLetterConfiguration() {
        val result = applicationTemplateValidationService.validateUpdate(
            1,
            ApplicationTemplateUpdateDto(
                id = null,
                name = "name",
                cvConfiguration = CvConfigurationRequestDto(
                    includedCvContent = null,
                    cvStyle = "talendo",
                    cvStyleOptions = null
                ),
                coverLetterConfiguration = CoverLetterConfigurationUpdateDto(
                    style = null,
                    language = null,
                    mirrorProfileImage = null,
                    content = null,
                    closing = null,
                    documents = null
                )
            ),
            profile
        )

        assertValidationResult(result, false, 1)
    }
}

package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.StringValidator
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.time.LocalDate

private val completeProfile = CVGenerationSnapshot(
    accountId = 1,
    isVerified = true,
    firstName = "Test",
    lastName = "User",
    email = "em@ail.com",
    phone = "+41 79 123 45 67",
    street = "My Home Street",
    houseNumber = "4",
    postcode = "12345",
    city = "City",
    birthday = LocalDate.of(1985, 6, 25),
    language = "en",
    profilePicture = "myPicture.png",
    jobTitle = "Test Job",
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
    skills = listOf(CVSkillSnapshot(id = 4, name = "Skill", type = "type", level = 5)),
)

private val unverifiedProfile = completeProfile.copy(isVerified = false)

private val incompleteProfile = completeProfile.copy(firstName = null)

class CvGeneratorValidationServiceTest {
    private lateinit var cvGeneratorValidationService: CvGeneratorValidationService

    @BeforeEach
    fun setup() {
        val messagesService: MessagesService = mockk(relaxed = true)
        val stringValidator = StringValidator(messagesService)
        cvGeneratorValidationService = CvGeneratorValidationService(messagesService, stringValidator)
    }

    @Test
    fun testValidateStyleOptions() {
        val options = mapOf(CVStyle.TALENDO.options.first().key to "#FFFFFF")
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        cvGeneratorValidationService.validateStyleOptions(CVStyle.TALENDO, options, validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateTemplateOptionsForStyleWithoutOptions() {
        val options = mapOf("invalid" to "invalid")
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        cvGeneratorValidationService.validateStyleOptions(CVStyle.MODERN, options, validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateStyleOptionsWithInvalidColor() {
        val options = mapOf(CVStyle.TALENDO.options.first().key to "invalid")
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        cvGeneratorValidationService.validateStyleOptions(CVStyle.TALENDO, options, validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
        assertEquals(1, validationErrorBuilder.build("").errors.size)
    }

    @Test
    fun testValidateProfileCompleteness() {
        val result = cvGeneratorValidationService.validateProfileCompleteness(completeProfile)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testValidateProfileCompletenessWithUnverifiedProfile() {
        val result = cvGeneratorValidationService.validateProfileCompleteness(unverifiedProfile)

        assertTrue { result.isFailure }
    }

    @Test
    fun testValidateProfileCompletenessWithIncompleteProfile() {
        val result = cvGeneratorValidationService.validateProfileCompleteness(incompleteProfile)

        assertTrue { result.isFailure }
    }

    @Test
    fun testValidateConfigurationWithInvalidStyle() {
        val config = CvConfigurationRequestDto(cvStyle = "invalid", includedCvContent = null, cvStyleOptions = null)

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ValidationException }
        val errors = (ex as ValidationException).errors
        assertEquals(1, errors.size)
    }

    @Test
    fun testValidateConfigurationWithInvalidStyleOptions() {
        val config = CvConfigurationRequestDto(
            cvStyle = CVStyle.TALENDO.styleKey,
            includedCvContent = null,
            cvStyleOptions = mapOf("invalid" to "invalid")
        )

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ValidationException }
        val errors = (ex as ValidationException).errors
        assertEquals(1, errors.size)
    }

    @Test
    fun testValidateConfigurationWithIncompleteCvContentConfig() {
        val config = CvConfigurationRequestDto(
            cvStyle = CVStyle.TALENDO.styleKey,
            includedCvContent = IncludedCvContentDto(null, null, null, null),
            cvStyleOptions = null
        )

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ValidationException }
        val errors = (ex as ValidationException).errors
        assertEquals(1, errors.size)
    }

    @Test
    fun testValidateConfigurationWithInvalidFilterIds() {
        val config =
            CvConfigurationRequestDto(
                cvStyle = CVStyle.MODERN.styleKey, includedCvContent = IncludedCvContentDto(
                    includedWorkExperience = listOf(IncludedCVItem(100, null)),
                    includedEducation = listOf(IncludedCVItem(100, null)),
                    includedProjects = listOf(IncludedCVItem(100, null)),
                    includedSkills = listOf(100)
                ),
                cvStyleOptions = null
            )

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ValidationException }
        val errors = (ex as ValidationException).errors
        assertEquals(4, errors.size)
    }

    @Test
    fun testValidateConfigurationWithNoFilterIds() {
        val config = CvConfigurationRequestDto(
            cvStyle = CVStyle.MODERN.styleKey,
            includedCvContent = IncludedCvContentDto(
                includedWorkExperience = emptyList(),
                includedEducation = emptyList(),
                includedProjects = emptyList(),
                includedSkills = emptyList()
            ),
            cvStyleOptions = null
        )

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ValidationException }
        val errors = (ex as ValidationException).errors
        assertEquals(1, errors.size)
    }

    @Test
    fun testValidateConfigurationWithNoContentConfig() {
        val config = CvConfigurationRequestDto(
            cvStyle = CVStyle.TALENDO.styleKey,
            includedCvContent = null,
            cvStyleOptions = mapOf(CVStyle.TALENDO.options.first().key to "#FFFFFF")
        )

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testValidateConfigurationWitNoStyleOptions() {
        val config = CvConfigurationRequestDto(
            cvStyle = CVStyle.TALENDO.styleKey,
            includedCvContent = IncludedCvContentDto(
                includedWorkExperience = listOf(element = IncludedCVItem(1, false)),
                includedEducation = listOf(IncludedCVItem(2, false)),
                includedProjects = listOf(IncludedCVItem(3, true)),
                includedSkills = listOf(4)
            ),
            cvStyleOptions = null
        )

        val result = cvGeneratorValidationService.validateConfiguration(config, completeProfile)

        assertTrue { result.isSuccess }
    }
}

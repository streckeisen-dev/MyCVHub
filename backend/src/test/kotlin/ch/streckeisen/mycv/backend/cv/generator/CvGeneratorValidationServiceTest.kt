package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
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

private val completeProfile = ProfileEntity(
    jobTitle = "Test Job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = true,
    isPhonePublic = true,
    isAddressPublic = true,
    hideDescriptions = false,
    profilePicture = "myPicture.png",
    id = 1,
    account = ApplicantAccountEntity(
        username = "testuser",
        password = null,
        isOAuthUser = true,
        isVerified = true,
        id = 1,
        accountDetails = AccountDetailsEntity(
            firstName = "Test",
            lastName = "User",
            email = "em@ail.com",
            phone = "+41 79 123 45 67",
            birthday = LocalDate.of(1985, 6, 25),
            street = "My Home Street",
            houseNumber = "4",
            postcode = "12345",
            city = "City",
            country = "CH",
            language = "en"
        )
    ),
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
)

private val unverifiedProfile = ProfileEntity(
    jobTitle = "Invalid Job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = false,
    isPhonePublic = false,
    isAddressPublic = false,
    hideDescriptions = false,
    profilePicture = "picture.png",
    id = 1,
    account = ApplicantAccountEntity(
        username = "unverified",
        password = null,
        isOAuthUser = true,
        isVerified = false,
        id = 5,
        accountDetails = mockk()
    ),
)

private val incompleteProfile = ProfileEntity(
    jobTitle = "Invalid Job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = false,
    isPhonePublic = false,
    isAddressPublic = false,
    hideDescriptions = false,
    profilePicture = "picture.png",
    id = 1,
    account = ApplicantAccountEntity(
        username = "unverified",
        password = null,
        isOAuthUser = true,
        isVerified = true,
        id = 5,
        accountDetails = null
    ),
)

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
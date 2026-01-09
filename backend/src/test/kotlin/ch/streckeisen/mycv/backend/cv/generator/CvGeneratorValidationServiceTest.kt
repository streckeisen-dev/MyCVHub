package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
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
    )
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
        cvGeneratorValidationService = CvGeneratorValidationService(messagesService)
    }

    @Test
    fun testValidateTemplateOptions() {
        val options = mapOf(CVStyle.TALENDO.options.first().key to "#FFFFFF")

        val result = cvGeneratorValidationService.validateTemplateOptions(CVStyle.TALENDO, options)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testValidateTemplateOptionsForTemplateWithoutOptions() {
        val options = mapOf("invalid" to "invalid")

        val result = cvGeneratorValidationService.validateTemplateOptions(CVStyle.MODERN, options)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is ValidationException)
        val errors = (ex as ValidationException).errors
        assertEquals(1, errors.size)
    }

    @Test
    fun testValidateTemplateOptionsWithInvalidColor() {
        val options = mapOf(CVStyle.TALENDO.options.first().key to "invalid")

        val result = cvGeneratorValidationService.validateTemplateOptions(CVStyle.TALENDO, options)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is ValidationException)
        val errors = (ex as ValidationException).errors
        assertEquals(1, errors.size)
    }

    @Test
    fun testVerifyProfileCompleteness() {
        val result = cvGeneratorValidationService.verifyProfileCompleteness(completeProfile)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testVerifyProfileCompletenessWithUnverifiedProfile() {
        val result = cvGeneratorValidationService.verifyProfileCompleteness(unverifiedProfile)

        assertTrue { result.isFailure }
    }

    @Test
    fun testVerifyProfileCompletenessWithIncompleteProfile() {
        val result = cvGeneratorValidationService.verifyProfileCompleteness(incompleteProfile)

        assertTrue { result.isFailure }
    }
}
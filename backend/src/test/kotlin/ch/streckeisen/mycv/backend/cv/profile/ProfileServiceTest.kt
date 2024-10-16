package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.LocalDate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val existingAccount =
    ApplicantAccountEntity(
        "f",
        "l",
        "email",
        "phone",
        LocalDate.now(),
        "street",
        null,
        "code",
        "city",
        "CH",
        "abc",
        1,
        profile = ProfileEntity(
            "alias",
            "job",
            "bio",
            isProfilePublic = false,
            isEmailPublic = false,
            isPhonePublic = false,
            isAddressPublic = false,
            profilePicture = "picture.jpg",
            id = 1,
            account = mockk()
        )
    )
private val existingProfile =
    ProfileEntity(
        "alias",
        "job",
        "bio",
        isProfilePublic = false,
        isEmailPublic = false,
        isPhonePublic = false,
        isAddressPublic = false,
        profilePicture = "picture.jpg",
        id = 1,
        account = existingAccount
    )

class ProfileServiceTest {
    private lateinit var profileRepository: ProfileRepository
    private lateinit var profileValidationService: ProfileValidationService
    private lateinit var applicantAccountService: ApplicantAccountService
    private lateinit var profilePictureService: ProfilePictureService
    private lateinit var profileService: ProfileService

    @BeforeEach
    fun setup() {
        profileRepository = mockk {
            every { save(any()) } returns existingProfile
        }
        profileValidationService = mockk()
        applicantAccountService = mockk {
            every { findById(eq(1)) } returns Result.success(existingAccount)
            every { findById(eq(2)) } returns Result.failure(ResultNotFoundException("Not found"))
        }
        profilePictureService = mockk()
        profileService =
            ProfileService(profileRepository, profileValidationService, applicantAccountService, profilePictureService)
    }

    @Test
    fun testProfileSave() {
        every {
            profileValidationService.validateProfileInformation(
                eq(1),
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)
        every { profilePictureService.store(eq(1), any(), any()) } returns Result.success("test.png")

        val update = GeneralProfileInformationUpdateDto(
            "test",
            "Job",
            null,
            isProfilePublic = true,
            isEmailPublic = false,
            isPhonePublic = false,
            isAddressPublic = false
        )

        val saveResult = profileService.save(1, update, mockk())

        assertTrue { saveResult.isSuccess }
        val entity = saveResult.getOrNull()
        assertNotNull(entity)
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any(), any()) }
        verify(exactly = 1) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 1) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveWithoutPicture() {
        every {
            profileValidationService.validateProfileInformation(
                eq(1),
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)

        val update = GeneralProfileInformationUpdateDto(
            "test",
            "Job",
            null,
            isProfilePublic = true,
            isEmailPublic = false,
            isPhonePublic = false,
            isAddressPublic = false
        )

        val saveResult = profileService.save(1, update, null)

        assertTrue { saveResult.isSuccess }
        val entity = saveResult.getOrNull()
        assertNotNull(entity)
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any(), any()) }
        verify(exactly = 0) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 1) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveAccountNotFound() {
        val saveResult = profileService.save(2, mockk(), mockk())

        assertTrue { saveResult.isFailure }
        val ex = saveResult.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is ResultNotFoundException }
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 0) { profileValidationService.validateProfileInformation(any(), any(), any(), any()) }
        verify(exactly = 0) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveValidationError() {
        every {
            profileValidationService.validateProfileInformation(
                eq(1),
                any(),
                any(),
                any()
            )
        } returns Result.failure(IllegalArgumentException("Validation error"))

        val saveResult = profileService.save(1, mockk(), mockk())

        assertTrue { saveResult.isFailure }
        val ex = saveResult.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IllegalArgumentException }

        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any(), any()) }
        verify(exactly = 0) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSavePictureError() {
        every {
            profileValidationService.validateProfileInformation(
                eq(1),
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)
        every { profilePictureService.store(any(), any(), any()) } returns Result.failure(IOException("File error"))

        val saveResult = profileService.save(1, mockk(), mockk())

        assertTrue { saveResult.isFailure }
        val ex = saveResult.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is IOException }
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any(), any()) }
        verify(exactly = 1) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 0) { profileRepository.save(any()) }
    }
}
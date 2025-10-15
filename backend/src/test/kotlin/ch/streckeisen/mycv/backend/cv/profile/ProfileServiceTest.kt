package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val existingAccount =
    ApplicantAccountEntity(
        username = "username",
        password = "abc",
        isOAuthUser = false,
        isVerified = true,
        accountDetails = AccountDetailsEntity(
            firstName = "f",
            lastName = "l",
            email = "ch/streckeisen/mycv/email",
            phone = "phone",
            birthday = LocalDate.now(),
            street = "street",
            houseNumber = null,
            postcode = "code",
            city = "city",
            country = "CH",
            language = "en"
        ),
        id = 1,
        profile = ProfileEntity(
            jobTitle = "job",
            bio = "bio",
            isProfilePublic = true,
            isEmailPublic = true,
            isPhonePublic = true,
            isAddressPublic = true,
            hideDescriptions = false,
            profilePicture = "picture.jpg",
            id = 1,
            account = mockk()
        )
    )
private val existingAccountWithoutProfile =
    ApplicantAccountEntity(
        username = "username",
        password = "abc",
        isOAuthUser = false,
        isVerified = true,
        accountDetails = AccountDetailsEntity(
            firstName = "f",
            lastName = "l",
            email = "ch/streckeisen/mycv/email",
            phone = "phone",
            birthday = LocalDate.now(),
            street = "street",
            houseNumber = null,
            postcode = "code",
            city = "city",
            country = "CH",
            "en"
        ),
        id = 3,
        profile = null
    )
private val existingProfile =
    ProfileEntity(
        jobTitle = "job",
        bio = "bio",
        isProfilePublic = false,
        isEmailPublic = false,
        isPhonePublic = false,
        isAddressPublic = false,
        hideDescriptions = true,
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
            every { findById(eq(2)) } returns Result.failure(IllegalArgumentException("Not found"))
            every { findById(eq(3)) } returns Result.success(existingAccountWithoutProfile)
        }
        profilePictureService = mockk()
        profileService =
            ProfileService(
                profileRepository,
                profileValidationService,
                applicantAccountService,
                profilePictureService
            )
    }

    @Test
    fun testProfileSave() {
        every {
            profileValidationService.validateProfileInformation(
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)
        every { profilePictureService.store(eq(1), any(), any()) } returns Result.success("test.png")

        val update = GeneralProfileInformationUpdateDto(
            jobTitle = "Job",
            bio = null,
            isProfilePublic = true,
            isEmailPublic = false,
            isPhonePublic = false,
            isAddressPublic = false,
            hideDescriptions = true
        )

        val saveResult = profileService.save(1, update, mockk())

        assertTrue { saveResult.isSuccess }
        val entity = saveResult.getOrNull()
        assertNotNull(entity)
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any()) }
        verify(exactly = 1) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 1) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveWithoutPicture() {
        every {
            profileValidationService.validateProfileInformation(
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)

        val update = GeneralProfileInformationUpdateDto(
            jobTitle = "Job",
            bio = null,
            isProfilePublic = true,
            isEmailPublic = false,
            isPhonePublic = false,
            isAddressPublic = false,
            hideDescriptions = false
        )

        val saveResult = profileService.save(1, update, null)

        assertTrue { saveResult.isSuccess }
        val entity = saveResult.getOrNull()
        assertNotNull(entity)
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any()) }
        verify(exactly = 0) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 1) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveAccountNotFound() {
        val saveResult = profileService.save(2, mockk(), mockk())

        assertTrue { saveResult.isFailure }
        val ex = saveResult.exceptionOrNull()
        assertNotNull(ex)
        verify(exactly = 1) { applicantAccountService.findById(any()) }
        verify(exactly = 0) { profileValidationService.validateProfileInformation(any(), any(), any()) }
        verify(exactly = 0) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveValidationError() {
        every {
            profileValidationService.validateProfileInformation(
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
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any()) }
        verify(exactly = 0) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSavePictureError() {
        every {
            profileValidationService.validateProfileInformation(
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
        verify(exactly = 1) { profileValidationService.validateProfileInformation(any(), any(), any()) }
        verify(exactly = 1) { profilePictureService.store(any(), any(), any()) }
        verify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun testProfileSaveDefaultValues() {
        val profileEntitySlot = slot<ProfileEntity>()
        every {
            profileValidationService.validateProfileInformation(
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)
        every { profilePictureService.store(eq(3), any(), any()) } returns Result.success("test.png")
        every { profileRepository.save(capture(profileEntitySlot)) } returns existingProfile

        val update = GeneralProfileInformationUpdateDto(
            jobTitle = "Job",
            bio = null,
            isProfilePublic = null,
            isEmailPublic = null,
            isPhonePublic = null,
            isAddressPublic = null,
            hideDescriptions = null
        )

        val saveResult = profileService.save(3, update, mockk())

        assertTrue { saveResult.isSuccess }
        assertNotNull(profileEntitySlot.captured)
        assertEquals(false, profileEntitySlot.captured.isProfilePublic)
        assertEquals(false, profileEntitySlot.captured.isEmailPublic)
        assertEquals(false, profileEntitySlot.captured.isPhonePublic)
        assertEquals(false, profileEntitySlot.captured.isAddressPublic)
        assertEquals(true, profileEntitySlot.captured.hideDescriptions)
    }

    @Test
    fun testProfileSaveKeepingExistingValues() {
        val profileEntitySlot = slot<ProfileEntity>()
        every {
            profileValidationService.validateProfileInformation(
                any(),
                any(),
                any()
            )
        } returns Result.success(Unit)
        every { profilePictureService.store(eq(1), any(), any()) } returns Result.success("test.png")
        every { profileRepository.save(capture(profileEntitySlot)) } returns existingProfile

        val update = GeneralProfileInformationUpdateDto(
            jobTitle = "Job",
            bio = null,
            isProfilePublic = null,
            isEmailPublic = null,
            isPhonePublic = null,
            isAddressPublic = null,
            hideDescriptions = null
        )

        val saveResult = profileService.save(1, update, mockk())

        assertTrue { saveResult.isSuccess }
        assertNotNull(profileEntitySlot.captured)
        assertEquals(true, profileEntitySlot.captured.isProfilePublic)
        assertEquals(true, profileEntitySlot.captured.isEmailPublic)
        assertEquals(true, profileEntitySlot.captured.isPhonePublic)
        assertEquals(true, profileEntitySlot.captured.isAddressPublic)
        assertEquals(false, profileEntitySlot.captured.hideDescriptions)
    }
}
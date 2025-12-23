package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

private val validTheme = ProfileThemeUpdateDto("#000000", "#111111")

class ProfileThemeServiceTest {
    private lateinit var profileThemeRepository: ProfileThemeRepository
    private lateinit var profileService: ProfileService
    private lateinit var profileThemeValidationService: ProfileThemeValidationService
    private lateinit var profileThemeService: ProfileThemeService
    private lateinit var profileThemeSlot: CapturingSlot<ProfileThemeEntity>

    @BeforeEach
    fun setup() {
        val profile = profile(null)
        profileThemeSlot = slot()
        profileThemeRepository = mockk {
            every { save(capture(profileThemeSlot)) } returns ProfileThemeEntity(
                "#000000",
                "#FFFFFF",
                profile,
                1
            )
        }
        profileService = mockk {
            every { findByAccountId(any()) } returns Result.failure(IllegalArgumentException("Not found"))
            every { findByAccountId(eq(1)) } returns Result.success(profile)
            every { findByAccountId(eq(2)) } returns Result.success(
                profile(
                    ProfileThemeEntity(
                        "#123456",
                        "#ABC321",
                        profile,
                        2
                    )
                )
            )
        }
        profileThemeValidationService = mockk {
            every { validateThemeUpdate(any()) } returns Result.failure(
                IllegalArgumentException("Illegal args")
            )
            every { validateThemeUpdate(eq(validTheme)) } returns Result.success(Unit)
        }
        profileThemeService = ProfileThemeService(profileThemeRepository, profileService, profileThemeValidationService)
    }

    @Test
    fun testSave() {
        val result = profileThemeService.save(1, validTheme)

        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())

        verify(exactly = 1) { profileService.findByAccountId(eq(1)) }
        verify(exactly = 1) { profileThemeValidationService.validateThemeUpdate(any()) }
        verify(exactly = 1) { profileThemeRepository.save(any()) }

        assertNotNull(profileThemeSlot.captured)
        assertEquals("#000000", profileThemeSlot.captured.backgroundColor)
        assertEquals("#111111", profileThemeSlot.captured.surfaceColor)
        assertNull(profileThemeSlot.captured.id)
    }

    @Test
    fun testUpdateExisting() {
        val result = profileThemeService.save(2, validTheme)

        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())

        verify(exactly = 1) { profileService.findByAccountId(eq(2)) }
        verify(exactly = 1) { profileThemeValidationService.validateThemeUpdate(any()) }
        verify(exactly = 1) { profileThemeRepository.save(any()) }

        assertNotNull(profileThemeSlot.captured)
        assertEquals("#000000", profileThemeSlot.captured.backgroundColor)
        assertEquals("#111111", profileThemeSlot.captured.surfaceColor)
        assertEquals(2, profileThemeSlot.captured.id)
    }

    @Test
    fun testSaveWithMissingProfile() {
        val result = profileThemeService.save(5, mockk())

        assertTrue { result.isFailure }
        verify(exactly = 1) { profileService.findByAccountId(eq(5)) }
        verify(exactly = 0) { profileThemeValidationService.validateThemeUpdate(any()) }
        verify(exactly = 0) { profileThemeRepository.save(any()) }
    }

    @Test
    fun testSaveWithInvalidTheme() {
        val result = profileThemeService.save(1, ProfileThemeUpdateDto(null, null))

        assertTrue { result.isFailure }
        verify(exactly = 1) { profileService.findByAccountId(eq(1)) }
        verify(exactly = 1) { profileThemeValidationService.validateThemeUpdate(any()) }
        verify(exactly = 0) { profileThemeRepository.save(any()) }
    }

    private fun profile(theme: ProfileThemeEntity?) = ProfileEntity(
        jobTitle = "jobTitle",
        bio = "Bio",
        isProfilePublic = true,
        isEmailPublic = false,
        isPhonePublic = false,
        isAddressPublic = false,
        hideDescriptions = false,
        profilePicture = "picture",
        id = 1,
        account = mockk(),
        profileTheme = theme
    )
}
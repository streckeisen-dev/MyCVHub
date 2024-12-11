package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ProfileEntityDeletionListenerTest {
    private lateinit var profilePictureService: ProfilePictureService
    private lateinit var profileEntityDeletionListener: ProfileEntityDeletionListener

    @BeforeEach
    fun setup() {
        profilePictureService = mockk()
        profileEntityDeletionListener = ProfileEntityDeletionListener(profilePictureService)
    }

    @Test
    fun testPreRemoveSuccess() {
        val picture = "validProfilePicture"
        val profile = mockk<ProfileEntity> {
            every { profilePicture } returns picture
            every { account } returns mockk {
                every { id } returns 1
            }
        }
        every { profilePictureService.delete(eq(1), eq(picture)) } returns Result.success(Unit)

        profileEntityDeletionListener.preRemove(profile)

        verify(exactly = 1) { profilePictureService.delete(eq(1), eq(picture)) }
    }

    @Test
    fun testPreRemoveFailure() {
        val picture = "validProfilePicture"
        val profile = mockk<ProfileEntity> {
            every { profilePicture
            } returns picture
            every { account } returns mockk {
                every { id } returns 1
            }
        }
        every { profilePictureService.delete(eq(1), eq(picture)) } returns Result.failure(RuntimeException())

        assertThrows<LocalizedException> { profileEntityDeletionListener.preRemove(profile) }

        verify(exactly = 1) { profilePictureService.delete(eq(1), eq(picture)) }
    }
}
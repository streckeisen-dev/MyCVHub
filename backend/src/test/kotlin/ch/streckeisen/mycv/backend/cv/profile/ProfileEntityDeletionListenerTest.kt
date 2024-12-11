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
        val profilePicture = "validProfilePicture"
        val profile = mockk<ProfileEntity> {
            every { profilePicture } returns profilePicture
            every { account } returns mockk {
                every { id } returns 1
            }
        }
        every { profilePictureService.delete(eq(1), eq(profilePicture)) } returns Result.success(Unit)

        profileEntityDeletionListener.preRemove(profile)

        verify(exactly = 1) { profilePictureService.delete(eq(1), eq(profilePicture)) }
    }

    @Test
    fun testPreRemoveFailure() {
        val profilePicture = "validProfilePicture"
        val profile = mockk<ProfileEntity> {
            every { profilePicture } returns profilePicture
            every { account } returns mockk {
                every { id } returns 1
            }
        }
        every { profilePictureService.delete(eq(1), eq(profilePicture)) } returns Result.failure(RuntimeException())

        assertThrows<LocalizedException> { profileEntityDeletionListener.preRemove(profile) }

        verify(exactly = 1) { profilePictureService.delete(eq(1), eq(profilePicture)) }
    }
}
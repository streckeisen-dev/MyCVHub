package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import jakarta.persistence.PreRemove
import org.springframework.stereotype.Component

@Component
class ProfileEntityDeletionListener(
    private val profilePictureService: ProfilePictureService
) {
    @PreRemove
    fun preRemove(profile: ProfileEntity) {
        profilePictureService.delete(profile.profilePicture)
            .onFailure {
                throw LocalizedException("${MYCV_KEY_PREFIX}.account.delete.cleanupError")
            }
    }
}
package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.PreRemove
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ProfileEntityDeletionListener(
    private val profilePictureService: ProfilePictureService
) {
    @PreRemove
    fun preRemove(profile: ProfileEntity) {
        profilePictureService.delete(profile.account.id!!, profile.profilePicture)
            .onFailure {
                logger.error(it) { "Account [${profile.account.id}]: Failed to delete profile picture '${profile.profilePicture}'" }
                throw LocalizedException("${MYCV_KEY_PREFIX}.account.delete.cleanupError")
            }
    }
}
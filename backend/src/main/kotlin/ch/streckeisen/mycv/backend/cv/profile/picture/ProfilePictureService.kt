package ch.streckeisen.mycv.backend.cv.profile.picture

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import org.apache.tika.mime.MediaType
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

private val allowedMediaTypes = listOf(MediaType.image("png"), MediaType.image("jpg"), MediaType.image("jpeg"))
private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

@Service
class ProfilePictureService(
    private val profilePictureStorageService: ProfilePictureStorageService
) {
    fun get(accountId: Long?, profile: ProfileEntity): Result<ProfilePicture> {
        if (!profile.isProfilePublic && profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You can't access this profile picture"))
        }

        val profilePicture = profilePictureStorageService.get(profile.profilePicture)
            .getOrElse { return Result.failure(it) }

        return Result.success(profilePicture)
    }

    fun store(accountId: Long, profilePicture: MultipartFile, oldProfilePicture: String?): Result<String> {
        if (profilePicture.isEmpty) {
            return Result.failure(IllegalArgumentException("Profile picture is empty"))
        }

        val mediaType = profilePictureStorageService.detectContentType(BufferedInputStream(profilePicture.inputStream))
        if (!allowedMediaTypes.contains(mediaType)) {
            return Result.failure(IllegalArgumentException("Profile picture media type is not allowed"))
        }

        val imageType = mediaType.subtype
        val profilePictureName = "$accountId.${imageType}"
        val savedProfilePicture = profilePictureStorageService.store(profilePictureName, profilePicture)
            .getOrElse { return Result.failure(it) }

        if (oldProfilePicture != null && savedProfilePicture != oldProfilePicture) {
            profilePictureStorageService.delete(oldProfilePicture)
                .onFailure {
                    // since this is a cleanup job, we don't want the action itself to fail
                    logger.error("Failed to delete old profile picture", it)
                }
        }

        return Result.success(savedProfilePicture)
    }
}
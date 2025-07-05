package ch.streckeisen.mycv.backend.cv.profile.picture

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.tika.mime.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream

private val allowedMediaTypes = listOf(MediaType.image("png"), MediaType.image("jpg"), MediaType.image("jpeg"))
private val logger = KotlinLogging.logger { }

private const val ACCESS_DENIED_MESSAGE = "${MYCV_KEY_PREFIX}.profile.picture.accessDenied"
private const val ILLEGAL_MEDIA_TYPE_MESSAGE = "${MYCV_KEY_PREFIX}.profile.validation.pictureIllegalMediaType"

private const val PROFILE_PICTURE_FIELD = "profilePicture"

@Service
class ProfilePictureService(
    private val profilePictureStorageService: ProfilePictureStorageService,
    private val messagesService: MessagesService,
) {
    fun get(accountId: Long?, profile: ProfileEntity): Result<ProfilePicture> {
        if (!profile.isProfilePublic && profile.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE))
        }

        val profilePicture = profilePictureStorageService.get(profile.profilePicture)
            .getOrElse { return Result.failure(it) }

        return Result.success(profilePicture)
    }

    fun getThumbnail(accountId: Long?, profile: ProfileEntity): Result<ProfilePicture> {
        if (profile.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE))
        }

        val profilePicture = profilePictureStorageService.getThumbnail(profile.profilePicture)
            .getOrElse { return Result.failure(it) }

        return Result.success(profilePicture)
    }

    fun getCVPicture(accountId: Long?, profile: ProfileEntity): Result<ProfilePicture> {
        if (profile.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE))
        }

        val profilePicture = profilePictureStorageService.getCVPicture(profile.profilePicture)
            .getOrElse { return Result.failure(it) }

        return Result.success(profilePicture)
    }

    fun store(accountId: Long, profilePicture: MultipartFile, oldProfilePicture: String?): Result<String> {
        if (profilePicture.isEmpty) {
            return Result.failure(IllegalArgumentException(messagesService.requiredFieldMissingError(
                PROFILE_PICTURE_FIELD
            )))
        }

        val mediaType = profilePictureStorageService.detectContentType(BufferedInputStream(profilePicture.inputStream))
        if (!allowedMediaTypes.contains(mediaType)) {
            return Result.failure(IllegalArgumentException(messagesService.getMessage(ILLEGAL_MEDIA_TYPE_MESSAGE)))
        }

        val savedProfilePicture = profilePictureStorageService.store(profilePicture)
            .getOrElse { return Result.failure(it) }

        if (oldProfilePicture != null && savedProfilePicture != oldProfilePicture) {
            delete(accountId, oldProfilePicture)
                .onFailure {
                    // since this is a cleanup job, we don't want the action itself to fail
                    logger.error(it) { "Account [$accountId]: Failed to delete old profile picture" }
                }
        }

        logger.debug { "Account [$accountId]: Stored profile picture '$savedProfilePicture'" }
        return Result.success(savedProfilePicture)
    }

    fun delete(accountId: Long, profilePicture: String): Result<Unit> {
        profilePictureStorageService.delete(profilePicture)
            .onFailure { return Result.failure(it) }

        logger.debug { "Account [$accountId]: Successfully deleted profile picture $profilePicture" }
        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.cv.profile.picture

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.files.FileService
import org.apache.tika.mime.MediaType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.name

private val allowedMediaTypes = listOf(MediaType.image("png"), MediaType.image("jpg"), MediaType.image("jpeg"))
private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

@Service
class ProfilePictureService(
    @Value("\${PROFILE_PICTURE_FOLDER}") private val profilePictureFolder: String,
    private val fileService: FileService
) {
    private val rootLocation: Path = Paths.get(profilePictureFolder)
        .normalize()
        .absolute()

    init {
        if (!rootLocation.exists()) {
            throw FileNotFoundException("Could not find $profilePictureFolder")
        }
    }

    fun get(accountId: Long?, profile: ProfileEntity): Result<Path> {
        if (!profile.isProfilePublic && profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You can't access this profile picture"))
        }

        val profilePicture = rootLocation.resolve(profile.profilePicture)
            .normalize()
        if (!profilePicture.exists()) {
            return Result.failure(ResultNotFoundException("Profile picture not found"))
        }

        return Result.success(profilePicture)
    }

    fun store(accountId: Long, profilePicture: MultipartFile, oldProfilePicture: String?): Result<String> {
        if (profilePicture.isEmpty) {
            return Result.failure(IllegalArgumentException("Profile picture is empty"))
        }

        val mediaType = fileService.detectContentType(BufferedInputStream(profilePicture.inputStream))
        if (!allowedMediaTypes.contains(mediaType)) {
            return Result.failure(IllegalArgumentException("Profile picture media type is not allowed"))
        }

        val imageType = mediaType.subtype
        val profilePicturePath =
            rootLocation.resolve(Paths.get("$accountId.${imageType}"))
                .normalize()
                .toAbsolutePath()

        if (!profilePicturePath.parent.equals(rootLocation)) {
            return Result.failure(IllegalStateException("Profile Picture path is outside of root directory"))
        }

        fileService.copyFile(profilePicture, profilePicturePath)
            .onFailure { return Result.failure(it) }

        if (oldProfilePicture != null && profilePicturePath.name != oldProfilePicture) {
            val previousPicture = rootLocation.resolve(Paths.get(oldProfilePicture))
            fileService.deleteFile(previousPicture)
                .onFailure {
                    // since this is a cleanup job, we don't want the action itself to fail
                    logger.error("Failed to delete old profile picture", it)
                }
        }

        return Result.success(profilePicturePath.name)
    }
}
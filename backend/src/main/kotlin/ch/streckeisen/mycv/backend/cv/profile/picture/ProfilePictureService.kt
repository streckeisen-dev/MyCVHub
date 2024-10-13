package ch.streckeisen.mycv.backend.cv.profile.picture

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.name

private val ALLOWED_MEDIA_TYPES = listOf(MediaType.image("png"), MediaType.image("jpg"), MediaType.image("jpeg"))

@Service
class ProfilePictureService(
    @Value("\${PROFILE_PICTURE_FOLDER}") private val profilePictureFolder: String
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

    fun store(accountId: Long, file: MultipartFile, oldProfilePicture: String?): Result<String> {
        if (file.isEmpty) {
            return Result.failure(IllegalArgumentException("Profile picture is empty"))
        }

        val mediaType = detectContentType(BufferedInputStream(file.inputStream))
        if (!ALLOWED_MEDIA_TYPES.contains(mediaType)) {
            return Result.failure(IllegalArgumentException("Profile picture media type is not allowed"))
        }

        val imageType = mediaType.subtype
        val profilePictureFile =
            rootLocation.resolve(Paths.get("$accountId.${imageType}"))
                .normalize()
                .toAbsolutePath()

        if (!profilePictureFile.parent.equals(rootLocation)) {
            return Result.failure(IllegalStateException("Profile Picture path is outside of root directory"))
        }

        file.inputStream.use { inputStream ->
            try {
                Files.copy(inputStream, profilePictureFile, StandardCopyOption.REPLACE_EXISTING)
            } catch (ex: IOException) {
                return Result.failure(ex)
            }
        }

        if (oldProfilePicture != null && profilePictureFile.name != oldProfilePicture) {
            val previousPicture = rootLocation.resolve(Paths.get(oldProfilePicture))
            try {
                Files.delete(previousPicture)
            } catch (ex: IOException) {
                return Result.failure(IOException("Failed to delete old profile picture", ex))
            }
        }

        return Result.success(profilePictureFile.name)
    }

    private fun detectContentType(stream: BufferedInputStream): MediaType {
        val detector = DefaultDetector()
        val metadata = Metadata()

        return detector.detect(stream, metadata)
    }
}
package ch.streckeisen.mycv.backend.cv.profile.picture

import com.cloudinary.Cloudinary
import com.cloudinary.EagerTransformation
import org.apache.commons.io.FileUtils
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URI
import java.time.Instant
import kotlin.io.path.createTempFile

private const val PROD_ASSET_FOLDER = "prod"
private const val TEST_ASSET_FOLDER = "local"
private const val AUTHENTICATED_TYPE = "authenticated"

private const val PROFILE_PICTURE_TRANSFORMATION = "profile_picture"
private const val PROFILE_PICTURE_THUMBNAIL_TRANSFORMATION = "profile_picture_thumbnail"

@Service
class ProfilePictureStorageService(
    @Value("\${cloudinary.api-key}") private val apiKey: String,
    @Value("\${cloudinary.api-secret}") private val apiSecret: String,
    @Value("\${cloudinary.cloud-name}") private val cloudName: String,
    @Value("\${cloudinary.url-expiration}") private val urlExpirationTime: Long,
    environment: Environment
) {
    private val cloudinary = Cloudinary("cloudinary://${apiKey}:${apiSecret}@${cloudName}")
    private val isProd = environment.activeProfiles.contains("prod")

    fun detectContentType(stream: BufferedInputStream): MediaType {
        val detector = DefaultDetector()
        val metadata = Metadata()

        return detector.detect(stream, metadata)
    }

    fun get(filename: String): Result<ProfilePicture> {
        val expiresAt = Instant.now().plusSeconds(urlExpirationTime).epochSecond
        val downloadParams = mapOf(
            "type" to AUTHENTICATED_TYPE,
            "expires_at" to expiresAt,
            "public_id" to filename,
        )

        try {
            val url = cloudinary.privateDownload(filename, "png", downloadParams)
            return Result.success(ProfilePicture(filename, URI(url)))
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    fun getThumbnail(filename: String): Result<ProfilePicture> {
        val expiresAt = Instant.now().plusSeconds(urlExpirationTime).epochSecond.toString()
        val params = mapOf(
            "public_id" to filename,
            "format" to "png",
            "type" to AUTHENTICATED_TYPE,
            "expires_at" to expiresAt,
            "timestamp" to (System.currentTimeMillis() / 1000L).toString(),
            "transformation" to "t_${PROFILE_PICTURE_THUMBNAIL_TRANSFORMATION}"
        )

        try {
            cloudinary.signRequest(params, params)
            val url = buildUrl(cloudinary.cloudinaryApiUrl("download", params), params)
            return Result.success(ProfilePicture(filename, URI(url)))
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    fun store(profilePicture: MultipartFile): Result<String> {
        val file = createTempFile("profile-picture", ".tmp").toFile()
        try {
            profilePicture.inputStream.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val transformation = EagerTransformation()
                .named(PROFILE_PICTURE_TRANSFORMATION)
                .generate()

            val uploadParams = mapOf(
                "type" to AUTHENTICATED_TYPE,
                "asset_folder" to if (isProd) PROD_ASSET_FOLDER else TEST_ASSET_FOLDER,
                "overwrite" to true,
                "transformation" to transformation
            )
            val result = cloudinary.uploader()
                .upload(file, uploadParams)
                .toMap()

            val publicId = result["public_id"] as String?
            if (publicId == null) {
                return Result.failure(ProfilePictureStorageException("Public ID of uploaded file not found"))
            }
            return Result.success(publicId)
        } catch (ex: IOException) {
            return Result.failure(ex)
        } finally {
            FileUtils.delete(file)
        }
    }

    fun delete(filename: String): Result<Unit> {
        val destroyParams = mapOf(
            "invalidate" to true,
            "type" to AUTHENTICATED_TYPE
        )
        val result = cloudinary.uploader()
            .destroy(filename, destroyParams)
            .toMap()

        val status = result["result"] as String?
        if (status != "ok") {
            return Result.failure(ProfilePictureStorageException("Failed to delete file $filename"))
        }
        return Result.success(Unit)
    }

    private fun buildUrl(base: String, params: Map<String, Any>): String {
        val urlBuilder = StringBuilder()
        urlBuilder.append(base)
        if (params.isNotEmpty()) {
            urlBuilder.append("?")
        }
        var isFirst = true
        for (param in params) {
            if (!isFirst) {
                urlBuilder.append("&")
            }
            urlBuilder.append(param.key)
                .append("=")
                .append(param.value.toString())
            isFirst = false
        }
        return urlBuilder.toString()
    }
}
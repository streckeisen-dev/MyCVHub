package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.io.FileUtils
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

private val logger = KotlinLogging.logger { }

@Service
class CVGeneratorService(
    private val profileService: ProfileService,
    private val profilePictureService: ProfilePictureService,
    private val messagesService: MessagesService,
    private val objectMapper: ObjectMapper
) {
    suspend fun generateCV(accountId: Long, cvStyle: CVStyle): Result<ByteArray> {
        val profile = profileService.findByAccountId(accountId)
            .onFailure { return Result.failure(it) }
            .getOrElse { return Result.failure(it) }

        val tempDir = createTempDirectory("cv_$accountId")
        try {
            val cvTemplate =
                this.javaClass.classLoader.getResourceAsStream("ch/streckeisen/mycv/backend/cv/${cvStyle.cvTemplate}.typ")
            if (cvTemplate == null) {
                return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.cv.templateNotFound"))
            }

            withContext(Dispatchers.IO) {
                cvTemplate.use { input ->
                    FileOutputStream(tempDir.resolve("${cvStyle.cvTemplate}.typ").toFile()).use { output ->
                        input.copyTo(output)
                    }
                }

                profilePictureService.getCVPicture(accountId, profile)
                    .onFailure { return@withContext Result.failure<ByteArray>(it) }
                    .onSuccess { profilePictureDto ->
                        profilePictureDto.uri.toURL().openStream().use {
                            FileUtils.copyInputStreamToFile(it, tempDir.resolve("profile.jpg").toFile())
                        }
                    }

                val cvProfile = profile.toCVProfile(LocaleContextHolder.getLocale(), messagesService)
                val profileJson = tempDir.resolve("profile.json").createFile()
                objectMapper.writeValue(profileJson.toFile(), cvProfile)
            }

            return compileCV(tempDir, cvStyle.cvTemplate, accountId)
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile())
        }
    }

    private suspend fun compileCV(dir: Path, template: String, accountId: Long): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            val outputPath = "${dir.pathString}/cv_${accountId}.pdf"
            val process = ProcessBuilder(
                "typst",
                "compile",
                "${dir.pathString}/${template}.typ",
                outputPath
            ).start()

            withTimeout(30_000) {
                process.onExit().await()
            }

            val resultCode = process.exitValue()
            if (resultCode != 0) {
                val error = process.errorStream.bufferedReader().use { it.readText() }
                logger.error { "Error while generating CV: $error" }
                return@withContext Result.failure(LocalizedException("ch.streckeisen.mycv.cv.generationFailed"))
            }
            val bytes = dir.resolve("cv_${accountId}.pdf").toFile().readBytes()
            return@withContext Result.success(bytes)
        }
}
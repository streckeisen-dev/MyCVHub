package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.util.isValidHexColor
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.io.FileUtils
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

private val logger = KotlinLogging.logger { }

private const val TEMPLATE_NOT_FOUND_MESSAGE = "$MYCV_KEY_PREFIX.cv.templateNotFound"
private const val GENERATION_FAILED_MESSAGE = "$MYCV_KEY_PREFIX.cv.generationFailed"
private const val INCOMPLETE_PROFILE_MESSAGE = "$MYCV_KEY_PREFIX.cv.incompleteProfile"
private const val NO_CV_ENTRIES_MESSAGE = "$MYCV_KEY_PREFIX.cv.noCvEntries"
private const val UNSUPPORTED_TEMPLATE_OPTIONS = "$MYCV_KEY_PREFIX.cv.templateOptions.unsupported"
private const val UNKNOWN_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.unknown"
private const val INVALID_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.invalid"

private const val PROFILE_PICTURE_FILE_NAME = "profile.jpg"
private const val PROFILE_JSON_FILE_NAME = "profile.json"

@Service
class CVGeneratorService(
    private val profileService: ProfileService,
    private val profilePictureService: ProfilePictureService,
    private val messagesService: MessagesService,
    private val objectMapper: ObjectMapper
) {
    suspend fun generateCV(
        accountId: Long,
        cvStyle: CVStyle,
        includedWorkExperience: List<IncludedCVItem>?,
        includedEduction: List<IncludedCVItem>?,
        includedProjects: List<IncludedCVItem>?,
        includedSkills: List<IncludedCVItem>?,
        templateOptions: Map<String, String>?
    ): Result<ByteArray> {
        if (templateOptions != null) {
            validateTemplateOptions(cvStyle, templateOptions)
        }

        val completeTemplateOptions = cvStyle.options.associate { option ->
            option.key to (templateOptions?.get(option.key) ?: option.defaultValue)
        }

        val profile = profileService.findByAccountId(accountId)
            .onFailure { return Result.failure(it) }
            .getOrElse { return Result.failure(it) }

        val workExperiences = profile.workExperiences.filter { w ->
            includedWorkExperience == null || includedWorkExperience.any {incl -> incl.id == w.id}
        }.map { w ->
            if (!includedWorkExperience?.find { incl -> incl.id == w.id }!!.includeDescription) {
                WorkExperienceEntity(
                    w.id,
                    w.jobTitle,
                    w.company,
                    w.positionStart,
                    w.positionEnd,
                    w.location,
                    "",
                    w.profile
                )
            } else w
        }
        val education = profile.education.filter { e ->
            includedEduction == null || includedEduction.any {incl -> incl.id == e.id}
        }
        val projects = profile.projects.filter { p ->
            includedProjects == null || includedProjects.any {incl -> incl.id == p.id}
        }
        val skills = profile.skills.filter { s ->
            includedSkills == null || includedSkills.any {incl -> incl.id == s.id}
        }

        val entryCount = workExperiences.size + education.size + projects.size + skills.size
        if (entryCount == 0) return Result.failure(LocalizedException(NO_CV_ENTRIES_MESSAGE))


        val tempDir = createTempDirectory("cv_$accountId")
        try {
            val cvTemplate =
                this.javaClass.classLoader.getResource("ch/streckeisen/mycv/backend/cv_templates/")?.toURI()
            if (cvTemplate == null) {
                return Result.failure(LocalizedException(TEMPLATE_NOT_FOUND_MESSAGE))
            }

            return withContext(Dispatchers.IO) {
                @OptIn(ExperimentalPathApi::class)
                Path.of(cvTemplate).copyToRecursively(tempDir, overwrite = true, followLinks = false)

                profilePictureService.getCVPicture(accountId, profile)
                    .onFailure { return@withContext Result.failure(it) }
                    .onSuccess { profilePictureDto ->
                        profilePictureDto.uri.toURL().openStream().use {
                            FileUtils.copyInputStreamToFile(it, tempDir.resolve(PROFILE_PICTURE_FILE_NAME).toFile())
                        }
                    }

                verifyProfileCompleteness(profile)
                    .onFailure { return@withContext Result.failure(it) }
                    .onSuccess {
                        val cvData = profile.toCVData(
                            LocaleContextHolder.getLocale(),
                            messagesService,
                            workExperiences,
                            education,
                            projects,
                            skills,
                            completeTemplateOptions
                        )
                        val profileJson = tempDir.resolve(PROFILE_JSON_FILE_NAME).createFile()
                        objectMapper.writeValue(profileJson.toFile(), cvData)
                    }

                return@withContext compileCV(tempDir, cvStyle.cvTemplate, accountId)
            }
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile())
        }
    }

    private fun verifyProfileCompleteness(profile: ProfileEntity): Result<Unit> {
        if (profile.account.accountDetails == null) {
            return Result.failure(LocalizedException(INCOMPLETE_PROFILE_MESSAGE))
        }

        return Result.success(Unit)
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
                logger.error { "[Account $accountId] Failed to generate CV: $error" }
                return@withContext Result.failure(LocalizedException(GENERATION_FAILED_MESSAGE))
            }
            val bytes = dir.resolve("cv_${accountId}.pdf").toFile().readBytes()
            logger.debug { "[Account $accountId] Successfully generated CV" }
            return@withContext Result.success(bytes)
        }

    private fun validateTemplateOptions(cvStyle: CVStyle, templateOptions: Map<String, String>) {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()
        if (cvStyle.options.isEmpty() && templateOptions.isNotEmpty()) {
            validationErrorBuilder.addError("templateOptions", UNSUPPORTED_TEMPLATE_OPTIONS)
        }

        templateOptions.keys.forEach { key ->
            val option = cvStyle.options.find { it.key == key }
            if (option == null) {
                validationErrorBuilder.addError("templateOptions.[$key]", UNKNOWN_TEMPLATE_OPTION)
            } else {
                if (option.type == CVStyleOptionType.COLOR) {
                    val color = templateOptions[key]!!
                    if (!isValidHexColor(color)) {
                        validationErrorBuilder.addError("templateOptions.[$key]", INVALID_TEMPLATE_OPTION)
                    }
                }
            }
        }

        if (validationErrorBuilder.hasErrors()) {
            throw validationErrorBuilder.build(INVALID_TEMPLATE_OPTION)
        }
    }
}
package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.generator.data.CVDataService
import ch.streckeisen.mycv.backend.cv.generator.typst.TypstService
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.util.isValidHexColor
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory

private const val TEMPLATE_NOT_FOUND_MESSAGE = "$MYCV_KEY_PREFIX.cv.templateNotFound"
private const val GENERATION_FAILED_MESSAGE = "$MYCV_KEY_PREFIX.cv.generationFailed"
private const val INCOMPLETE_PROFILE_MESSAGE = "$MYCV_KEY_PREFIX.cv.incompleteProfile"
private const val NO_CV_ENTRIES_MESSAGE = "$MYCV_KEY_PREFIX.cv.noCvEntries"
private const val UNSUPPORTED_TEMPLATE_OPTIONS = "$MYCV_KEY_PREFIX.cv.templateOptions.unsupported"
private const val UNKNOWN_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.unknown"
private const val MISSING_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.missing"
private const val INVALID_TEMPLATE_OPTION = "$MYCV_KEY_PREFIX.cv.templateOptions.invalid"

private const val PROFILE_PICTURE_FILE_NAME = "profile.jpg"
private const val PROFILE_JSON_FILE_NAME = "profile.json"

@Service
class CVGeneratorService(
    private val profileService: ProfileService,
    private val profilePictureService: ProfilePictureService,
    private val objectMapper: ObjectMapper,
    private val typstService: TypstService,
    private val cvDataService: CVDataService
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
                .onFailure { return Result.failure(it) }
        }

        val completeTemplateOptions = cvStyle.options.associate { option ->
            option.key to (templateOptions?.get(option.key) ?: option.defaultValue)
        }

        val profile = profileService.findByAccountId(accountId)
            .onFailure { return Result.failure(it) }
            .getOrElse { return Result.failure(it) }

        verifyProfileCompleteness(profile)
            .onFailure { return Result.failure(it) }

        val workExperiences = cvDataService.prepareWorkExperiences(profile.workExperiences, includedWorkExperience)
        val education = cvDataService.prepareEducation(profile.education, includedEduction)
        val projects = cvDataService.prepareProjects(profile.projects, includedProjects)
        val skills = cvDataService.prepareSkills(profile.skills, includedSkills)

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

                val cvData = cvDataService.createCVData(
                    LocaleContextHolder.getLocale(),
                    profile,
                    workExperiences,
                    education,
                    projects,
                    skills,
                    completeTemplateOptions
                )
                val profileJson = tempDir.resolve(PROFILE_JSON_FILE_NAME).createFile()
                objectMapper.writeValue(profileJson.toFile(), cvData)

                return@withContext typstService.compile(tempDir, "${cvStyle.cvTemplate}.typ", "cv_$accountId.pdf")
                    .fold(
                        onSuccess = { Result.success(it) },
                        onFailure = { Result.failure(LocalizedException(GENERATION_FAILED_MESSAGE)) }
                    )
            }
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile())
        }
    }

    private fun verifyProfileCompleteness(profile: ProfileEntity): Result<Unit> {
        if (!profile.account.isVerified || profile.account.accountDetails == null) {
            return Result.failure(LocalizedException(INCOMPLETE_PROFILE_MESSAGE))
        }

        return Result.success(Unit)
    }

    private fun validateTemplateOptions(cvStyle: CVStyle, templateOptions: Map<String, String>): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()
        if (cvStyle.options.isEmpty() && templateOptions.isNotEmpty()) {
            validationErrorBuilder.addError("templateOptions", UNSUPPORTED_TEMPLATE_OPTIONS)
        }

        templateOptions.keys.forEach { key ->
            val option = cvStyle.options.find { it.key == key }
            if (option == null) {
                validationErrorBuilder.addError("templateOptions.[$key]", UNKNOWN_TEMPLATE_OPTION)
            } else {
                validateOption(option, templateOptions[key]!!, validationErrorBuilder)
            }
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(INVALID_TEMPLATE_OPTION))
        }
        return Result.success(Unit)
    }

    private fun validateOption(option: CVStyleOption, value: String, validationErrorBuilder: ValidationException.ValidationErrorBuilder) {
        when (option.type) {
            CVStyleOptionType.COLOR -> {
                if (!isValidHexColor(value)) {
                    validationErrorBuilder.addError("templateOptions.[${option.key}]", INVALID_TEMPLATE_OPTION)
                }
            }

            CVStyleOptionType.STRING -> {
                if (StringUtils.isBlank(value)) {
                    validationErrorBuilder.addError("templateOptions.[${option.key}]", MISSING_TEMPLATE_OPTION)
                }
            }
        }
    }
}
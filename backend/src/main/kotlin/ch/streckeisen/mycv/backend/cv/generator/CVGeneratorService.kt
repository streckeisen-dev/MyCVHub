package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.generator.data.CVDataService
import ch.streckeisen.mycv.backend.cv.generator.typst.TypstService
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory

private const val TEMPLATE_NOT_FOUND_MESSAGE = "$MYCV_KEY_PREFIX.cv.templateNotFound"
private const val GENERATION_FAILED_MESSAGE = "$MYCV_KEY_PREFIX.cv.generationFailed"

private const val PROFILE_PICTURE_FILE_NAME = "profile.jpg"
private const val PROFILE_JSON_FILE_NAME = "profile.json"

private const val CV_TEMPLATES_LOCATION = "ch/streckeisen/mycv/backend/templates/cv/"

@Service
class CVGeneratorService(
    private val cvGeneratorValidationService: CvGeneratorValidationService,
    private val profileService: ProfileService,
    private val profilePictureService: ProfilePictureService,
    private val objectMapper: ObjectMapper,
    private val typstService: TypstService,
    private val cvDataService: CVDataService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun generateCV(accountId: Long, cvConfiguration: CvConfigurationRequestDto): Result<ByteArray> {
        val profile = profileService.findByAccountId(accountId)
            .getOrElse { return Result.failure(it) }

        cvGeneratorValidationService.validateProfileCompleteness(profile)
            .onFailure { return Result.failure(it) }

        cvGeneratorValidationService.validateConfiguration(cvConfiguration, profile)
            .onFailure { return Result.failure(it) }

        val cvStyle = CVStyle.fromStyleKey(cvConfiguration.cvStyle)!!

        val completeStyleOptions = cvStyle.options.associate { option ->
            option.key to (cvConfiguration.cvStyleOptions?.get(option.key) ?: option.defaultValue)
        }

        val workExperiences = cvDataService.filterWorkExperiences(
            profile.workExperiences,
            cvConfiguration.includedCvContent?.includedWorkExperience
        )
        val education = cvDataService.filterEducation(
            profile.education,
            cvConfiguration.includedCvContent?.includedEducation
        )
        val projects = cvDataService.filterProjects(
            profile.projects,
            cvConfiguration.includedCvContent?.includedProjects
        )
        val skills = cvDataService.filterSkills(
            profile.skills,
            cvConfiguration.includedCvContent?.includedSkills
        )

        val tempDir = createTempDirectory("cv_$accountId")
        try {
            val cvTemplate =
                this.javaClass.classLoader.getResource(CV_TEMPLATES_LOCATION)?.toURI()
            if (cvTemplate == null) {
                return Result.failure(LocalizedException(TEMPLATE_NOT_FOUND_MESSAGE))
            }

            return withContext(dispatcher) {
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
                    profile,
                    workExperiences,
                    education,
                    projects,
                    skills,
                    completeStyleOptions
                )
                val profileJson = tempDir.resolve(PROFILE_JSON_FILE_NAME).createFile()
                objectMapper.writeValue(profileJson.toFile(), cvData)

                return@withContext typstService.compile(tempDir, "${cvStyle.styleKey}.typ", "cv_$accountId.pdf")
                    .fold(
                        onSuccess = { Result.success(it) },
                        onFailure = { Result.failure(LocalizedException(GENERATION_FAILED_MESSAGE)) }
                    )
            }
        } finally {
            FileUtils.deleteDirectory(tempDir.toFile())
        }
    }
}
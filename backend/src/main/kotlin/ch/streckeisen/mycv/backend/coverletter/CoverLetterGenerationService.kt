package ch.streckeisen.mycv.backend.coverletter

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.generator.typst.TypstService
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

private const val CL_TEMPLATES_LOCATION = "ch/streckeisen/mycv/backend/templates/cover_letter/"
private const val PROFILE_PICTURE_FILE_NAME = "profile.jpg"
private const val CL_CONFIG_FILE_NAME = "config.json"

private const val CL_MSG_PREFIX = "$MYCV_KEY_PREFIX.coverletter"
private const val TEMPLATE_NOT_FOUND_MESSAGE = "$CL_MSG_PREFIX.templateNotFound"
private const val GENERATION_FAILED_MESSAGE = "$CL_MSG_PREFIX.generationFailed"
private const val INCOMPLETE_ACCOUNT_MESSAGE = "$CL_MSG_PREFIX.incompleteAccount"

@Service
class CoverLetterGenerationService(
    private val coverLetterGenerationValidationService: CoverLetterGenerationValidationService,
    private val applicantAccountService: ApplicantAccountService,
    private val profilePictureService: ProfilePictureService,
    private val objectMapper: ObjectMapper,
    private val typstService: TypstService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun generateCoverLetter(accountId: Long, request: CoverLetterGenerationRequestDto): Result<ByteArray> {
        coverLetterGenerationValidationService.validateGenerationRequest(request)
            .onFailure { return Result.failure(it) }

        val style = CoverLetterStyle.fromStyleKey(request.style)!!

        val account = applicantAccountService.findByIdForCoverLetterGeneration(accountId)
            .getOrElse { return Result.failure(it) }

        if (account.isIncomplete()
        ) {
            return Result.failure(LocalizedException(INCOMPLETE_ACCOUNT_MESSAGE))
        }

        val tempWorkingDir = createTempDirectory("cl_$accountId")
        try {
            val coverLetterTemplate =
                this.javaClass.classLoader.getResource(CL_TEMPLATES_LOCATION)?.toURI()
            if (coverLetterTemplate == null) {
                return Result.failure(LocalizedException(TEMPLATE_NOT_FOUND_MESSAGE))
            }

            return withContext(dispatcher) {
                @OptIn(ExperimentalPathApi::class)
                Path.of(coverLetterTemplate).copyToRecursively(tempWorkingDir, overwrite = true, followLinks = false)

                profilePictureService.getCVPicture(accountId, account.accountId, account.profilePicture!!)
                    .onFailure { return@withContext Result.failure(it) }
                    .onSuccess { profilePictureDto ->
                        profilePictureDto.uri.toURL().openStream().use {
                            FileUtils.copyInputStreamToFile(
                                it,
                                tempWorkingDir.resolve(PROFILE_PICTURE_FILE_NAME).toFile()
                            )
                        }
                    }

                val authorAddressPart1 = "${account.street}${
                    if (account.houseNumber == null) {
                        ""
                    } else {
                        " " + account.houseNumber
                    }
                }"
                val authorAddressPart2 = "${account.postcode} ${account.city}"
                val data = CoverLetterData(
                    language = request.language!!,
                    mirrorProfileImage = request.mirrorProfileImage ?: false,
                    author = CoverLetterAuthor(
                        firstName = account.firstName!!,
                        lastName = account.lastName!!,
                        jobTitle = account.jobTitle!!,
                        email = account.email!!,
                        phone = account.phone!!,
                        address = "$authorAddressPart1, $authorAddressPart2"
                    ),
                    application = CoverLetterApplication(
                        jobTitle = request.application!!.jobTitle!!,
                        company = request.application.company!!,
                        contactPerson = if (request.application.contactPerson != null) {
                            CoverLetterContactPerson(
                                firstName = request.application.contactPerson.firstName!!,
                                lastName = request.application.contactPerson.lastName!!
                            )
                        } else null,
                        addressee = request.application.addressee,
                        salutation = request.application.salutation!!,
                        companyAddress = CoverLetterCompanyAddress(
                            line1 = request.application.companyAddress!!.street!!,
                            line2 = "${request.application.companyAddress.postcode} ${request.application.companyAddress.city}"
                        ),
                        content = request.application.content!!,
                        closing = request.application.closing!!
                    ),
                    documents = request.documents?.map { it!! }
                )
                val configJson = tempWorkingDir.resolve(CL_CONFIG_FILE_NAME).createFile()
                objectMapper.writeValue(configJson.toFile(), data)

                return@withContext typstService.compile(tempWorkingDir, "${style.styleKey}.typ", "cv_$accountId.pdf")
                    .fold(
                        onSuccess = { Result.success(it) },
                        onFailure = { Result.failure(LocalizedException(GENERATION_FAILED_MESSAGE)) }
                    )
            }
        } catch (ex: Exception) {
            return Result.failure(ex)
        } finally {
            FileUtils.deleteDirectory(tempWorkingDir.toFile())
        }
    }
}

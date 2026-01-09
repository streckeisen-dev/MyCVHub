package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.generator.data.CVData
import ch.streckeisen.mycv.backend.cv.generator.data.CVDataService
import ch.streckeisen.mycv.backend.cv.generator.data.CVEntry
import ch.streckeisen.mycv.backend.cv.generator.typst.TypstService
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePicture
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

private const val TALENDO_TEMPLATE = "talendo.typ"
private const val OUTPUT_PDF = "cv_1.pdf"

private val invalidProfile = ProfileEntity(
    jobTitle = "Invalid Job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = false,
    isPhonePublic = false,
    isAddressPublic = false,
    hideDescriptions = false,
    profilePicture = "picture.png",
    id = 1,
    account = mockk(),
)

private val completeProfile = ProfileEntity(
    jobTitle = "Test Job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = true,
    isPhonePublic = true,
    isAddressPublic = true,
    hideDescriptions = false,
    profilePicture = "myPicture.png",
    id = 1,
    account = ApplicantAccountEntity(
        username = "testuser",
        password = null,
        isOAuthUser = true,
        isVerified = true,
        id = 1,
        accountDetails = AccountDetailsEntity(
            firstName = "Test",
            lastName = "User",
            email = "em@ail.com",
            phone = "+41 79 123 45 67",
            birthday = LocalDate.of(1985, 6, 25),
            street = "My Home Street",
            houseNumber = "4",
            postcode = "12345",
            city = "City",
            country = "CH",
            language = "en"
        )
    ),
    workExperiences = listOf(
        WorkExperienceEntity(
            id = 1,
            jobTitle = "Current Job",
            company = "Tech Inc.",
            positionStart = LocalDate.of(2020, 5, 1),
            positionEnd = null,
            location = "Here",
            description = "Tech Stuff",
            profile = mockk()
        )
    ),
    education = emptyList(),
    projects = emptyList(),
    skills = emptyList(),
)

class CVGeneratorServiceTest {
    private lateinit var cvGeneratorValidationService: CvGeneratorValidationService
    private lateinit var profileService: ProfileService
    private lateinit var profilePictureService: ProfilePictureService
    private lateinit var typstService: TypstService
    private lateinit var cvGeneratorService: CVGeneratorService
    private lateinit var cvDataService: CVDataService

    @BeforeEach
    fun setup() {
        cvGeneratorValidationService = mockk {
            every { validateProfileCompleteness(eq(completeProfile)) } returns Result.success(Unit)
            every { validateProfileCompleteness(eq(invalidProfile)) } returns Result.failure(IllegalArgumentException())
        }

        profileService = mockk {
            every { findByAccountId(eq(1)) } returns Result.success(completeProfile)
            every { findByAccountId(eq(5)) } returns Result.success(invalidProfile)
        }

        profilePictureService = mockk {
            every { getCVPicture(eq(1), any()) } returns Result.success(
                ProfilePicture(
                    "profile.jpg",
                    this.javaClass.classLoader.getResource("profile.JPG")!!.toURI()
                )
            )
        }

        cvDataService = mockk(relaxed = true) {
            val delegate = CVDataService(mockk(relaxed = true))
            every { filterWorkExperiences(any(), any()) } answers {
                delegate.filterWorkExperiences(
                    firstArg(),
                    secondArg()
                )
            }
            every { createCVData(any(), any(), any(), any(), any(), any()) } returns CVData(
                "en",
                "Test",
                "User",
                "Job",
                "About Me",
                "em@il.com",
                "phone",
                "address",
                "birthday",
                listOf(
                    CVEntry(
                        "title",
                        "loc",
                        "start",
                        "end",
                        "institute",
                        "descr",
                        emptyList()
                    )
                ),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyMap()
            )
        }

        typstService = mockk()

        cvGeneratorService = CVGeneratorService(
            cvGeneratorValidationService,
            profileService,
            profilePictureService,
            ObjectMapper(),
            typstService,
            cvDataService
        )
    }

    @Test
    fun testCVGenerationWithoutCustomization() = runTest {
        coEvery { typstService.compile(any(), eq(TALENDO_TEMPLATE), eq(OUTPUT_PDF)) } returns Result.success(
            ByteArray(
                10
            )
        )

        val result = cvGeneratorService.generateCV(1, CVStyle.TALENDO, null, null, null, null, null)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testCVGenerationWithWorkExperienceFilter() = runTest {
        coEvery { typstService.compile(any(), eq(TALENDO_TEMPLATE), eq(OUTPUT_PDF)) } returns Result.success(
            ByteArray(
                10
            )
        )

        val result = cvGeneratorService.generateCV(
            1,
            CVStyle.TALENDO,
            listOf(IncludedCVItem(1, true)),
            null,
            null,
            null,
            null
        )

        assertTrue { result.isSuccess }
    }

    @Test
    fun testCVGenerationWithWorkExperienceFilterAndNoEntries() = runTest {
        coEvery { typstService.compile(any(), eq(TALENDO_TEMPLATE), eq(OUTPUT_PDF)) } returns Result.success(
            ByteArray(
                10
            )
        )

        val result = cvGeneratorService.generateCV(
            1,
            CVStyle.TALENDO,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            null
        )

        assertTrue { result.isFailure }
    }

    @Test
    fun testCVGenerationWithIncompleteProfile() = runTest {
        val result = cvGeneratorService.generateCV(5, CVStyle.MODERN, null, null, null, null, null)

        assertTrue { result.isFailure }
    }

    @Test
    fun testCVGenerationWithInvalidTemplateOptions() = runTest {
        val options = mapOf(CVStyle.TALENDO.options.first().key to "invalid")
        every { cvGeneratorValidationService.validateTemplateOptions(any(), eq(options)) } returns Result.failure(
            IllegalArgumentException()
        )

        val result = cvGeneratorService.generateCV(
            1,
            CVStyle.TALENDO,
            null,
            null,
            null,
            null,
            options
        )

        assertTrue { result.isFailure }
    }

    @Test
    fun testCVGenerationWithNonexistentTemplateOptions() = runTest {
        val options = mapOf("invalid" to "invalid")
        every { cvGeneratorValidationService.validateTemplateOptions(any(), eq(options)) } returns Result.failure(
            IllegalArgumentException()
        )

        val result = cvGeneratorService.generateCV(
            1,
            CVStyle.TALENDO,
            null,
            null,
            null,
            null,
            options
        )

        assertTrue { result.isFailure }
    }

    @Test
    fun testCVGenerationWithUnsupportedTemplateOptions() = runTest {
        val options = mapOf("invalid" to "invalid")
        every { cvGeneratorValidationService.validateTemplateOptions(any(), eq(options)) } returns Result.failure(
            IllegalArgumentException()
        )

        val result = cvGeneratorService.generateCV(
            1,
            CVStyle.MODERN,
            null,
            null,
            null,
            null,
            options
        )

        assertTrue { result.isFailure }
    }

    @Test
    fun testCVGenerationWithValidTemplateOptions() = runTest {
        coEvery { typstService.compile(any(), eq(TALENDO_TEMPLATE), eq(OUTPUT_PDF)) } returns Result.success(
            ByteArray(
                10
            )
        )
        val options = mapOf(CVStyle.TALENDO.options.first().key to "#FFFFFF")
        every {
            cvGeneratorValidationService.validateTemplateOptions(
                eq(CVStyle.TALENDO),
                eq(options)
            )
        } returns Result.success(Unit)

        val result = cvGeneratorService.generateCV(
            1,
            CVStyle.TALENDO,
            null,
            null,
            null,
            null,
            options
        )

        assertTrue { result.isSuccess }
    }
}
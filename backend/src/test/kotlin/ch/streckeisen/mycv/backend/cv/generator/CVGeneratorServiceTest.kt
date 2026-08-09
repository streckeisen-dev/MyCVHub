package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.generator.data.CVData
import ch.streckeisen.mycv.backend.cv.generator.data.CVDataService
import ch.streckeisen.mycv.backend.cv.generator.data.CVEntry
import ch.streckeisen.mycv.backend.cv.generator.typst.TypstService
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePicture
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

private val invalidProfile = CVGenerationSnapshot(
    accountId = 5,
    isVerified = false,
    firstName = null,
    lastName = null,
    email = null,
    phone = null,
    street = null,
    houseNumber = null,
    postcode = null,
    city = null,
    birthday = null,
    language = null,
    profilePicture = "picture.png",
    jobTitle = "Invalid Job",
    bio = null,
    workExperiences = emptyList(),
    education = emptyList(),
    projects = emptyList(),
    skills = emptyList()
)

private val completeProfile = CVGenerationSnapshot(
    accountId = 1,
    isVerified = true,
    firstName = "Test",
    lastName = "User",
    email = "em@ail.com",
    phone = "+41 79 123 45 67",
    street = "My Home Street",
    houseNumber = "4",
    postcode = "12345",
    city = "City",
    birthday = LocalDate.of(1985, 6, 25),
    language = "en",
    profilePicture = "myPicture.png",
    jobTitle = "Test Job",
    bio = null,
    workExperiences = listOf(
        CVWorkExperienceSnapshot(
            id = 1,
            jobTitle = "Current Job",
            company = "Tech Inc.",
            positionStart = LocalDate.of(2020, 5, 1),
            positionEnd = null,
            location = "Here",
            description = "Tech Stuff"
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
            every { findByAccountIdForCVGeneration(any()) } returns Result.failure(IllegalArgumentException())
            every { findByAccountIdForCVGeneration(eq(1)) } returns Result.success(completeProfile)
            every { findByAccountIdForCVGeneration(eq(5)) } returns Result.success(invalidProfile)
        }

        profilePictureService = mockk {
            every { getCVPicture(eq(1), eq(1), any()) } returns Result.success(
                ProfilePicture(
                    "profile.jpg",
                    this.javaClass.classLoader.getResource("profile.png")!!.toURI()
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
    suspend fun testCVGenerationWithInvalidProfile() {
        val result = cvGeneratorService.generateCV(50, mockk())

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testCvGenerationWithIncompleteProfile() {
        val result = cvGeneratorService.generateCV(5, mockk())

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testCvGenerationWithInvalidConfiguration() {
        val invalidConfig = mockk<CvConfigurationRequestDto>()
        every { cvGeneratorValidationService.validateConfiguration(invalidConfig, any<CVGenerationSnapshot>()) } returns Result.failure(
            IllegalArgumentException()
        )

        val result = cvGeneratorService.generateCV(1, invalidConfig)

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testCvGenerationWithProfilePictureNotFound() {
        val config = CvConfigurationRequestDto(
            cvStyle = "modern",
            includedCvContent = null,
            cvStyleOptions = null
        )
        every { cvGeneratorValidationService.validateConfiguration(config, any<CVGenerationSnapshot>()) } returns Result.success(Unit)
        every { profilePictureService.getCVPicture(1, 1, any()) } returns Result.failure(IllegalArgumentException())

        val result = cvGeneratorService.generateCV(1, config)

        assertTrue { result.isFailure }
        coVerify(exactly = 0) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testCvGenerationCvCompilationFails() {
        val config = CvConfigurationRequestDto(
            cvStyle = "modern",
            includedCvContent = null,
            cvStyleOptions = null
        )
        every { cvGeneratorValidationService.validateConfiguration(config, any<CVGenerationSnapshot>()) } returns Result.success(Unit)
        coEvery { typstService.compile(any(), any(), any()) } returns Result.failure(IllegalArgumentException())

        val result = cvGeneratorService.generateCV(1, config)

        assertTrue { result.isFailure }
        coVerify(exactly = 1) { typstService.compile(any(), any(), any()) }
    }

    @Test
    suspend fun testSuccessfulCvGeneration() {
        val config = CvConfigurationRequestDto(
            cvStyle = "modern",
            includedCvContent = null,
            cvStyleOptions = null
        )
        every { cvGeneratorValidationService.validateConfiguration(config, any<CVGenerationSnapshot>()) } returns Result.success(Unit)
        coEvery { typstService.compile(any(), any(), any()) } returns Result.success(ByteArray(10))

        val result = cvGeneratorService.generateCV(1, config)

        assertTrue { result.isSuccess }
        coVerify(exactly = 1) { typstService.compile(any(), any(), any()) }
    }
}

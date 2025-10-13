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
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

class CVGeneratorServiceTest {
    private lateinit var profileService: ProfileService
    private lateinit var profilePictureService: ProfilePictureService
    private lateinit var typstService: TypstService
    private lateinit var cvGeneratorService: CVGeneratorService
    private lateinit var cvDataService: CVDataService

    @BeforeEach
    fun setup() {
        profileService = mockk {
            every { findByAccountId(eq(1)) } returns Result.success(completeProfile())
            every { findByAccountId(eq(5)) } returns Result.success(invalidProfile())
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
            every { prepareWorkExperiences(any(), any()) } answers { delegate.prepareWorkExperiences(firstArg(), secondArg()) }
            every { createCVData(any(), any(), any(), any(), any(), any(), any()) } returns CVData(
                "en",
                "Test",
                "User",
                "Job",
                "About Me",
                "em@il.com",
                "phone",
                "address",
                "birthday",
                listOf(CVEntry(
                    "title",
                    "loc",
                    "start",
                    "end",
                    "institute",
                    "descr",
                    emptyList()
                )),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyMap()
            )
        }

        typstService = mockk()

        cvGeneratorService = CVGeneratorService(
            profileService,
            profilePictureService,
            ObjectMapper(),
            typstService,
            cvDataService
        )
    }

    @Test
    fun testCVGenerationWithoutCustomization() = runTest {
        coEvery { typstService.compile(any(), eq("talendo.typ"), eq("cv_1.pdf")) } returns Result.success(ByteArray(10))

        val result = cvGeneratorService.generateCV(1, CVStyle.TALENDO, null, null, null, null, null)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testCVGenerationWithWorkExperienceFilter() = runTest {
        coEvery { typstService.compile(any(), eq("talendo.typ"), eq("cv_1.pdf")) } returns Result.success(ByteArray(10))

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
        coEvery { typstService.compile(any(), eq("talendo.typ"), eq("cv_1.pdf")) } returns Result.success(ByteArray(10))

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
        coEvery { typstService.compile(any(), eq("talendo.typ"), eq("cv_1.pdf")) } returns Result.success(ByteArray(10))

        val options = mapOf(CVStyle.TALENDO.options.first().key to "#FFFFFF")

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

    private fun completeAndVerifiedAccount() = ApplicantAccountEntity(
        "testuser",
        null,
        true,
        true,
        1,
        AccountDetailsEntity(
            "Test",
            "User",
            "em@ail.com",
            "+41 79 123 45 67",
            LocalDate.of(1985, 6, 25),
            "My Home Street",
            "4",
            "12345",
            "City",
            "CH"
        )
    )

    private fun workExperiences() = listOf(
        WorkExperienceEntity(
            1,
            "Current Job",
            "Tech Inc.",
            LocalDate.of(2020, 5, 1),
            null,
            "Here",
            "Tech Stuff",
            mockk()
        ),
        WorkExperienceEntity(
            2,
            "Previous Job",
            "Other Inc.",
            LocalDate.of(2010, 1, 1),
            LocalDate.of(2015, 12, 31),
            "There",
            "Other Stuff",
            mockk()
        )
    )

    private fun completeProfile() = ProfileEntity(
        "Test Job",
        null,
        false,
        true,
        true,
        true,
        false,
        "myPicture.png",
        1,
        completeAndVerifiedAccount(),
        workExperiences = workExperiences(),
        education = emptyList(),
        projects = emptyList(),
        skills = emptyList(),
    )

    private fun unverifiedAccount() = ApplicantAccountEntity(
        "unverified",
        null,
        true,
        false,
        5,
        null
    )

    private fun invalidProfile() = ProfileEntity(
        "Invalid Job",
        null,
        false,
        false,
        false,
        false,
        false,
        "picture.png",
        1,
        unverifiedAccount(),
        workExperiences = workExperiences(),
    )
}
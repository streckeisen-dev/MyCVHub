package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.applicationTemplate.dto.ApplicationTemplateUpdateDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CoverLetterConfigurationUpdateDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CoverLetterConfigurationDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CvConfigurationDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.CvEntrySelectionDto
import ch.streckeisen.mycv.backend.applicationTemplate.dto.IncludedCvContentDto as TemplateIncludedCvContentDto
import ch.streckeisen.mycv.backend.cv.generator.CVGenerationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CvConfigurationRequestDto
import ch.streckeisen.mycv.backend.cv.generator.IncludedCVItem
import ch.streckeisen.mycv.backend.cv.generator.IncludedCvContentDto
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.Optional

private val existingProfile = ProfileEntity(
    id = 1,
    jobTitle = "job",
    bio = null,
    isProfilePublic = false,
    isEmailPublic = false,
    isPhonePublic = false,
    isAddressPublic = false,
    hideDescriptions = false,
    profilePicture = "picture.png",
    account = mockk {
        every { id } returns 1L
    }
)

private val existingProfileSnapshot = CVGenerationSnapshot(
    accountId = 1,
    isVerified = true,
    firstName = "Test",
    lastName = "User",
    email = "test@example.test",
    phone = "+41 79 123 45 67",
    street = "Street",
    houseNumber = null,
    postcode = "1234",
    city = "City",
    birthday = LocalDate.of(1990, 1, 1),
    language = "en",
    profilePicture = "picture.png",
    jobTitle = "job",
    bio = null,
    workExperiences = emptyList(),
    education = emptyList(),
    projects = emptyList(),
    skills = emptyList()
)

private val existingTemplate = ApplicationTemplateEntity(
    id = 1,
    name = "First template",
    cvConfiguration = """
        {
            "cvStyle": "talendo",
            "includedCvContent": {
                "includedWorkExperience": [
                    {
                        "entityId": 1,
                        "includeDescription": false
                    }
                ],
                "includedEducation": [],
                "includedProjects": [],
                "includedSkills": [5]
            },
            "cvStyleOptions": {
                "bannerBackground": "#FFFFFF"
            }
        }
    """.trimIndent(),
    coverLetterConfiguration = """
        {
            "style": "modern",
            "language": "en",
            "mirrorProfileImage": true,
            "content": "c",
            "closing": "d",
            "documents": ["e"]
            
        }
    """.trimIndent(),
    account = mockk {
        every { id } returns 1L
        every { profile } returns existingProfile
    }
)

private val validNewRequest = ApplicationTemplateUpdateDto(
    id = null,
    name = "new name",
    cvConfiguration = CvConfigurationRequestDto(
        includedCvContent = IncludedCvContentDto(
            includedWorkExperience = listOf(IncludedCVItem(1, null)),
            includedEducation = emptyList(),
            includedProjects = emptyList(),
            includedSkills = listOf(1),
        ),
        cvStyle = "talendo",
        cvStyleOptions = mapOf("bannerBackground" to "#FFFFFF")
    ),
    coverLetterConfiguration = CoverLetterConfigurationUpdateDto(
        style = "modern",
        language = "en",
        mirrorProfileImage = false,
        content = "c",
        closing = "d",
        documents = listOf("Uni Degree")
    )
)

class ApplicationTemplateServiceTest {
    private lateinit var templateSlot: CapturingSlot<ApplicationTemplateEntity>

    private lateinit var applicationTemplateRepository: ApplicationTemplateRepository
    private lateinit var profileService: ProfileService
    private lateinit var applicationTemplateValidationService: ApplicationTemplateValidationService
    private lateinit var applicationTemplateService: ApplicationTemplateService

    @BeforeEach
    fun setup() {
        templateSlot = slot()

        applicationTemplateRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(existingTemplate)

            every { findByAccountId(eq(1L)) } returns listOf(existingTemplate)

            every { save(capture(templateSlot)) } answers {
                val arg = firstArg<ApplicationTemplateEntity>()
                ApplicationTemplateEntity(100, arg.account, arg.name, arg.cvConfiguration, arg.coverLetterConfiguration)
            }

            every { delete(any()) } just runs
        }

        profileService = mockk {
            every { findByAccountIdForCVGeneration(any()) } returns Result.failure(IllegalArgumentException())
            every { findByAccountIdForCVGeneration(eq(1L)) } returns Result.success(existingProfileSnapshot)
            every { findEntityByAccountId(any()) } returns Result.failure(IllegalArgumentException())
            every { findEntityByAccountId(eq(1L)) } returns Result.success(existingProfile)
        }

        applicationTemplateValidationService = mockk {
            every { validateUpdate(any(), any(), any()) } returns Result.failure(IllegalArgumentException())
            every { validateUpdate(any(), eq(validNewRequest), any()) } returns Result.success(Unit)
        }

        val objectMapper = ObjectMapper()
        applicationTemplateService = ApplicationTemplateService(
            applicationTemplateRepository,
            profileService,
            applicationTemplateValidationService,
            objectMapper
        )
    }

    @Test
    fun testReadOfApplicationTemplateJsonFields() {
        val results = applicationTemplateService.findApplicationTemplates(1)

        assertEquals(1, results.size)
        val template = results.first()
        assertEquals(existingTemplate.id, template.id)
        assertEquals(existingTemplate.name, template.name)
        assertEquals(
            CvConfigurationDto(
                includedCvContent = TemplateIncludedCvContentDto(
                    includedWorkExperience = listOf(CvEntrySelectionDto(1, false)),
                    includedEducation = emptyList(),
                    includedProjects = emptyList(),
                    includedSkills = listOf(5)
                ),
                cvStyle = "talendo",
                cvStyleOptions = mapOf("bannerBackground" to "#FFFFFF")
            ), template.cvConfiguration
        )
        assertEquals(
            CoverLetterConfigurationDto(
                style = "modern",
                language = "en",
                mirrorProfileImage = true,
                content = "c",
                closing = "d",
                documents = listOf("e")
            ), template.coverLetterConfiguration
        )
    }

    @Test
    fun testFindByIdWithNonexistentTemplate() {
        val result = applicationTemplateService.findById(1, 10)

        assertTrue { result.isFailure }
    }

    @Test
    fun testFindByIdWithUnauthorizedAccount() {
        val result = applicationTemplateService.findById(2, 1)

        assertTrue { result.isFailure }
    }

    @Test
    fun testFindById() {
        val result = applicationTemplateService.findById(1, 1)

        assertTrue { result.isSuccess }
    }

    @Test
    fun testSaveExistingTemplateWithWrongId() {
        val result = applicationTemplateService.save(
            accountId = 1, applicationTemplate = ApplicationTemplateUpdateDto(
                id = 5,
                name = null,
                cvConfiguration = null,
                coverLetterConfiguration = null
            )
        )

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationTemplateRepository.save(any()) }
    }

    @Test
    fun testSaveExistingTemplateWithUnauthorizedAccount() {
        val result = applicationTemplateService.save(
            accountId = 2,
            applicationTemplate = ApplicationTemplateUpdateDto(
                id = 1,
                name = null,
                cvConfiguration = null,
                coverLetterConfiguration = null
            )
        )

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationTemplateRepository.save(any()) }
    }

    @Test
    fun testSaveWithInvalidUpdate() {
        val result = applicationTemplateService.save(1, ApplicationTemplateUpdateDto(null, null, null, null))

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationTemplateRepository.save(any()) }
    }

    @Test
    fun testSaveWithValidNewTemplate() {
        val result = applicationTemplateService.save(1, validNewRequest)

        assertTrue { result.isSuccess }
        assertNotNull(templateSlot.captured)
        assertNull(templateSlot.captured.id)
        assertEquals(validNewRequest.name, templateSlot.captured.name)
        assertEquals(
            "{\"includedCvContent\":{\"includedWorkExperience\":[{\"entityId\":1,\"includeDescription\":true}],\"includedEducation\":[],\"includedProjects\":[],\"includedSkills\":[1]},\"cvStyle\":\"talendo\",\"cvStyleOptions\":{\"bannerBackground\":\"#FFFFFF\"}}",
            templateSlot.captured.cvConfiguration
        )

        assertEquals("{\"style\":\"modern\",\"language\":\"en\",\"mirrorProfileImage\":false,\"content\":\"c\",\"closing\":\"d\",\"documents\":[\"Uni Degree\"]}", templateSlot.captured.coverLetterConfiguration)
    }

    @Test
    fun testDeleteOfNotExistingTemplate() {
        val result = applicationTemplateService.delete(1, 10)

        assertTrue { result.isFailure }
    }

    @Test
    fun testDeleteOfUnauthorizedTemplate() {
        val result = applicationTemplateService.delete(2, 1)

        assertTrue { result.isFailure }
    }

    @Test
    fun testDelete() {
        val result = applicationTemplateService.delete(1, 1)

        assertTrue { result.isSuccess }
    }
}

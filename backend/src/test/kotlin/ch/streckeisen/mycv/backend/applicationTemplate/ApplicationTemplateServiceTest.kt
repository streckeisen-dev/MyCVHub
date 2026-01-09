package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import tools.jackson.databind.ObjectMapper
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

private val existingTemplate = ApplicationTemplateEntity(
    id = 1,
    name = "First template",
    cvConfiguration = """
        {
            "cvTemplate": "talendo",
            "includedWorkExperience": [
                {
                    "entityId": 1,
                    "includeDescription": false
                }
            ],
            "includedSkills": [5],
            "templateParameters": {
                "bannerBackground": "#FFFFFF"
            }
        }
    """.trimIndent(),
    documentChecklist = null,
    account = mockk {
        every { id } returns 1L
        every { profile } returns existingProfile
    }
)

private val validNewRequest = ApplicationTemplateUpdateDto(
    id = null,
    name = "new name",
    cvConfiguration = CvConfigurationUpdateDto(
        includedWorkExperience = listOf(CvEntrySelectionUpdateDto(1, null)),
        includedEducation = null,
        includedProjects = null,
        includedSkills = listOf(1),
        cvTemplate = "talendo",
        templateOptions = mapOf("bannerBackground" to "#FFFFFF")
    ),
    documentChecklist = listOf("Uni Degree")
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
                ApplicationTemplateEntity(100, arg.account, arg.name, arg.cvConfiguration, arg.documentChecklist)
            }
        }

        profileService = mockk {
            every { findByAccountId(any()) } returns Result.failure(IllegalArgumentException())
            every { findByAccountId(eq(1L)) } returns Result.success(existingProfile)
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
            CvConfiguration(
                listOf(CvEntrySelection(1, false)),
                null,
                null,
                listOf(5),
                "talendo",
                mapOf("bannerBackground" to "#FFFFFF")
            ), template.cvConfiguration
        )
        assertNull(template.documentChecklist)
    }

    @Test
    fun testSaveExistingTemplateWithWrongId() {
        val result = applicationTemplateService.save(
            accountId = 1, applicationTemplate = ApplicationTemplateUpdateDto(
                id = 5,
                name = null,
                cvConfiguration = null,
                documentChecklist = null
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
                documentChecklist = null
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
        assertEquals("{\"includedWorkExperience\":[{\"entityId\":1,\"includeDescription\":true}],\"includedEducation\":null,\"includedProjects\":null,\"includedSkills\":[1],\"cvTemplate\":\"talendo\",\"templateParameters\":{\"bannerBackground\":\"#FFFFFF\"}}", templateSlot.captured.cvConfiguration)
        assertEquals("[\"Uni Degree\"]", templateSlot.captured.documentChecklist)
    }
}
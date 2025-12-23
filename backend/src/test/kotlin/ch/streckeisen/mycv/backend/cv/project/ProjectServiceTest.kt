package ch.streckeisen.mycv.backend.cv.project

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional

class ProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectValidationService: ProjectValidationService
    private lateinit var profileService: ProfileService
    private lateinit var projectService: ProjectService

    @BeforeEach
    fun setup() {
        projectRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(5)) } returns Optional.of(mockk {
                every { id } returns 5
                every { profile } returns mockk {
                    every { id } returns 50
                    every { account } returns mockk {
                        every { id } returns 2
                    }
                }
            })
            every { save(any()) } returns mockk()
            every { delete(any()) } returns Unit
        }
        projectValidationService = mockk {
            every { validateProject(any()) } returns Result.success(Unit)
        }
        profileService = mockk {
            every { findByAccountId(any()) } returns Result.failure(mockk())
            every { findByAccountId(eq(1)) } returns Result.success(mockk {
                every { id } returns 30
                every { account } returns mockk {
                    every { id } returns 1
                }
            })
        }
        projectService = ProjectService(projectRepository, projectValidationService, profileService)
    }

    @Test
    fun testSaveWithInvalidUpdateDto() {
        val updateDto = mockk<ProjectUpdateDto>()
        every { projectValidationService.validateProject(updateDto) } returns Result.failure(mockk())

        val result = projectService.save(1, updateDto)

        assertNotNull(result)
        assertTrue { result.isFailure }
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testSaveOfNotExistingProject() {
        val updateDto = mockk<ProjectUpdateDto> {
            every { id } returns 10
        }

        val result = projectService.save(1, updateDto)

        assertNotNull(result)
        assertTrue { result.isFailure }
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testSaveOfUnauthorizedExistingProject() {
        val updateDto = mockk<ProjectUpdateDto> {
            every { id } returns 5
        }

        val result = projectService.save(1, updateDto)

        assertNotNull(result)
        assertTrue { result.isFailure }
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testSaveOfExistingProject() {
        val updateDto = ProjectUpdateDto(
            id = 5,
            name = "na",
            role = "ro",
            description = "de",
            projectStart = LocalDate.of(2025, 1, 28),
            projectEnd = null,
            links = emptyList()
        )
        val saveSlot = slot<ProjectEntity>()
        every { projectRepository.save(capture(saveSlot)) } returns mockk()

        val result = projectService.save(2, updateDto)

        assertNotNull(result)
        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())

        assertNotNull(saveSlot.captured)
        assertEquals(5, saveSlot.captured.id)
        assertEquals("na", saveSlot.captured.name)
        assertEquals("ro", saveSlot.captured.role)
        assertEquals("de", saveSlot.captured.description)
        assertEquals(LocalDate.of(2025, 1, 28), saveSlot.captured.projectStart)
        assertEquals(null, saveSlot.captured.projectEnd)
        assertTrue { saveSlot.captured.links.isEmpty() }
        assertEquals(50, saveSlot.captured.profile.id)
    }

    @Test
    fun testSaveOfNewProjectWithMissingProfile() {
        val updateDto = mockk<ProjectUpdateDto> {
            every { id } returns null
        }

        val result = projectService.save(2, updateDto)

        assertNotNull(result)
        assertTrue { result.isFailure }
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testSaveOfNewProject() {
        val updateDto = ProjectUpdateDto(
            id = null,
            name = "nn",
            role = "nr",
            description = "nd",
            projectStart = LocalDate.of(2024, 11, 5),
            projectEnd = LocalDate.of(2025, 1, 17),
            links = listOf(ProjectLinkUpdateDto(url = "gh", displayName = "GitHub Link", type = ProjectLinkType.GITHUB))
        )
        val saveSlot = slot<ProjectEntity>()
        every { projectRepository.save(capture(saveSlot)) } returns mockk()

        val result = projectService.save(1, updateDto)

        assertNotNull(result)
        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())

        assertNotNull(saveSlot.captured)
        assertEquals(null, saveSlot.captured.id)
        assertEquals("nn", saveSlot.captured.name)
        assertEquals("nr", saveSlot.captured.role)
        assertEquals("nd", saveSlot.captured.description)
        assertEquals(LocalDate.of(2024, 11, 5), saveSlot.captured.projectStart)
        assertEquals(LocalDate.of(2025, 1, 17), saveSlot.captured.projectEnd)
        assertEquals(1, saveSlot.captured.links.size)
        assertEquals("gh", saveSlot.captured.links[0].url)
        assertEquals("GitHub Link", saveSlot.captured.links[0].displayName)
        assertEquals(ProjectLinkType.GITHUB, saveSlot.captured.links[0].type)
        assertEquals(30, saveSlot.captured.profile.id)
    }

    @Test
    fun testDeleteOfNotExistingProject() {
        val result = projectService.delete(1, 10)

        assertNotNull(result)
        assertTrue { result.isFailure }
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testDeleteOfUnauthorizedProject() {
        val result = projectService.delete(1, 5)

        assertNotNull(result)
        assertTrue { result.isFailure }
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testDelete() {
        val result = projectService.delete(2, 5)

        assertNotNull(result)
        assertTrue { result.isSuccess }
    }
}
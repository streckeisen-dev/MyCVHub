package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional

class WorkExperienceServiceTest {
    private lateinit var workExperienceRepository: WorkExperienceRepository
    private lateinit var workExperienceValidationService: WorkExperienceValidationService
    private lateinit var profileService: ProfileService
    private lateinit var workExperienceService: WorkExperienceService

    @BeforeEach
    fun setup() {
        val mockProfile = mockk<ProfileEntity> {
            every { id } returns 1
            every { account } returns mockk {
                every { id } returns 1
            }
        }

        workExperienceRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(mockk {
                every { id } returns 1
                every { profile } returns mockProfile
            })
            every { save(any()) } answers {
                firstArg<WorkExperienceEntity>().also { it.id = it.id ?: 100 }
            }
            every { delete(any()) } returns Unit
        }
        workExperienceValidationService = mockk()
        profileService = mockk {
            every { findEntityByAccountId(any()) } returns Result.failure(IllegalArgumentException(""))
            every { findEntityByAccountId(eq(1)) } returns Result.success(mockProfile)
        }
        workExperienceService =
            WorkExperienceService(workExperienceRepository, workExperienceValidationService, profileService)
    }

    @Test
    fun testSaveExistingExperienceNotFound() {
        val result = workExperienceService.save(1, mockk {
            every { id } returns 5
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)

        verify(exactly = 1) { workExperienceRepository.findById(eq(5)) }
        verify(exactly = 0) { workExperienceRepository.save(any()) }
    }

    @Test
    fun testSaveExistingExperienceWithWrongAccount() {
        val result = workExperienceService.save(5, mockk {
            every { id } returns 1
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)

        verify(exactly = 1) { workExperienceRepository.findById(eq(1)) }
        verify(exactly = 0) { profileService.findEntityByAccountId(any()) }
        verify(exactly = 0) { workExperienceRepository.save(any()) }
    }

    @Test
    fun testSaveInvalidAccount() {
        val result = workExperienceService.save(5, mockk {
            every { id } returns null
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)

        verify(exactly = 0) { workExperienceRepository.findById(any()) }
        verify(exactly = 0) { workExperienceRepository.save(any()) }
    }

    @Test
    fun testSaveInvalidUpdate() {
        every { workExperienceValidationService.validateWorkExperience(any()) } returns Result.failure(
            IllegalArgumentException()
        )

        val result = workExperienceService.save(1, mockk {
            every { id } returns null
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is IllegalArgumentException)

        verify(exactly = 1) { workExperienceValidationService.validateWorkExperience(any()) }
        verify(exactly = 0) { workExperienceRepository.save(any()) }
    }

    @Test
    fun testSave() {
        every { workExperienceValidationService.validateWorkExperience(any()) } returns Result.success(Unit)

        val result = workExperienceService.save(
            1,
            WorkExperienceUpdateDto(null, "Job", "Loc", "Company", LocalDate.now().minusDays(1), null, "description")
        )

        assertTrue { result.isSuccess }
        val entity = result.getOrNull()
        assertNotNull(entity)

        verify(exactly = 1) { workExperienceValidationService.validateWorkExperience(any()) }
        verify(exactly = 1) { workExperienceRepository.save(any()) }
    }

    @Test
    fun testDeleteWorkExperienceNotFound() {
        val result = workExperienceService.delete(1, 5)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
    }

    @Test
    fun testDeleteWrongAccount() {
        val result = workExperienceService.delete(5, 1)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
    }

    @Test
    fun testDelete() {
        val result = workExperienceService.delete(1, 1)

        assertTrue { result.isSuccess }
    }
}

package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
            every { save(any()) } returns mockk()
            every { delete(any()) } returns Unit
        }
        workExperienceValidationService = mockk()
        profileService = mockk {
            every { findByAccountId(any()) } returns Result.failure(EntityNotFoundException(""))
            every { findByAccountId(eq(1)) } returns Result.success(mockProfile)
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
        assertTrue { ex is EntityNotFoundException }

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
        assertTrue { ex is AccessDeniedException }

        verify(exactly = 1) { workExperienceRepository.findById(eq(1)) }
        verify(exactly = 0) { profileService.findByAccountId(any()) }
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
        assertTrue(ex is EntityNotFoundException)

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
        assertTrue(ex is EntityNotFoundException)
    }

    @Test
    fun testDeleteWrongAccount() {
        val result = workExperienceService.delete(5, 1)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is AccessDeniedException)
    }

    @Test
    fun testDelete() {
        val result = workExperienceService.delete(1, 1)

        assertTrue { result.isSuccess }
    }
}
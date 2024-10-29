package ch.streckeisen.mycv.backend.cv.education

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

class EducationServiceTest {
    private lateinit var educationRepository: EducationRepository
    private lateinit var educationValidationService: EducationValidationService
    private lateinit var profileService: ProfileService
    private lateinit var educationService: EducationService

    @BeforeEach
    fun setup() {
        val mockProfile = mockk<ProfileEntity> {
            every { id } returns 1
            every { account } returns mockk {
                every { id } returns 1
            }
        }

        educationRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(mockk {
                every { id } returns 1
                every { profile } returns mockProfile
            })
            every { save(any()) } returns mockk()
            every { delete(any()) } returns Unit
        }
        educationValidationService = mockk()
        profileService = mockk {
            every { findByAccountId(any()) } returns Result.failure(EntityNotFoundException("Profile not found"))
            every { findByAccountId(eq(1)) } returns Result.success(mockProfile)
        }
        educationService = EducationService(educationRepository, educationValidationService, profileService)
    }

    @Test
    fun testSaveExistingEducationNotFound() {
        val result = educationService.save(1, mockk {
            every { id } returns 5
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is EntityNotFoundException }

        verify(exactly = 1) { educationRepository.findById(eq(5)) }
        verify(exactly = 0) { educationRepository.save(any()) }
    }

    @Test
    fun testSaveExistingEducationWithWrongAccount() {
        val result = educationService.save(5, mockk {
            every { id } returns 1
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue { ex is AccessDeniedException }

        verify(exactly = 1) { educationRepository.findById(eq(1)) }
        verify(exactly = 0) { profileService.findByAccountId(any()) }
        verify(exactly = 0) { educationRepository.save(any()) }
    }

    @Test
    fun testSaveInvalidAccount() {
        val result = educationService.save(5, mockk {
            every { id } returns null
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is EntityNotFoundException)

        verify(exactly = 0) { educationRepository.findById(any()) }
        verify(exactly = 0) { educationRepository.save(any()) }
    }

    @Test
    fun testSaveInvalidUpdate() {
        every { educationValidationService.validateEducation(any()) } returns Result.failure(IllegalArgumentException())

        val result = educationService.save(1, mockk {
            every { id } returns null
        })

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is IllegalArgumentException)

        verify(exactly = 1) { educationValidationService.validateEducation(any()) }
        verify(exactly = 0) { educationRepository.save(any()) }
    }

    @Test
    fun testSave() {
        every { educationValidationService.validateEducation(any()) } returns Result.success(Unit)

        val result = educationService.save(
            1,
            EducationUpdateDto(null, "Inst", "Loc", LocalDate.now().minusDays(1), null, "deg", "desc")
        )

        assertTrue { result.isSuccess }
        val entity = result.getOrNull()
        assertNotNull(entity)

        verify(exactly = 1) { educationValidationService.validateEducation(any()) }
        verify(exactly = 1) { educationRepository.save(any()) }
    }

    @Test
    fun testDeleteEducationNotFound() {
        val result = educationService.delete(1, 5)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is EntityNotFoundException)
    }

    @Test
    fun testDeleteWrongAccount() {
        val result = educationService.delete(5, 1)

        assertTrue { result.isFailure }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is AccessDeniedException)
    }

    @Test
    fun testDelete() {
        val result = educationService.delete(1, 1)

        assertTrue { result.isSuccess }
    }
}
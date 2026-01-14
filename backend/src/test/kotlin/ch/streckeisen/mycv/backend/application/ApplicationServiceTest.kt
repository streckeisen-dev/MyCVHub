package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.application.dto.ScheduledWorkExperienceDto
import ch.streckeisen.mycv.backend.scheduled.SchedulerService
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

private val validAccount = ApplicantAccountEntity(
    username = "testuser",
    password = "pw",
    isOAuthUser = false,
    isVerified = true,
    id = 1
)

private val secondAccount = ApplicantAccountEntity(
    username = "seconduser",
    password = "pw",
    isOAuthUser = false,
    isVerified = true,
    id = 2
)

private val existingApplication = ApplicationEntity(
    id = 1,
    jobTitle = "a job",
    company = "a company",
    status = ApplicationStatus.WAITING_FOR_FIRST_RESPONSE,
    createdAt = LocalDateTime.of(2025, 12, 1, 12, 15),
    source = null,
    description = null,
    account = validAccount,
    history = listOf(
        ApplicationHistoryEntity(
            1,
            ApplicationStatus.UNSENT,
            ApplicationStatus.WAITING_FOR_FIRST_RESPONSE,
            null,
            LocalDateTime.of(2025, 12, 1, 12, 15),
            mockk()
        )
    )
)


private val existingApplicationTwo = ApplicationEntity(
    id = 2,
    jobTitle = "another job",
    company = "another comp",
    status = ApplicationStatus.WITHDRAWN,
    createdAt = LocalDateTime.of(2025, 12, 1, 15, 19),
    updatedAt = LocalDateTime.of(2025, 12, 20, 7, 5),
    source = null,
    description = "test",
    account = secondAccount
)

private val existingApplicationThree = ApplicationEntity(
    id = 3,
    jobTitle = "job",
    company = "comp",
    status = ApplicationStatus.OFFER_RECEIVED,
    createdAt = LocalDateTime.of(2025, 12, 1, 15, 19),
    updatedAt = LocalDateTime.of(2025, 12, 20, 7, 5),
    source = null,
    description = "test",
    account = secondAccount
)

private val validUpdateRequestWithoutId = ApplicationUpdateDto(
    null,
    "job",
    "comp",
    null,
    null
)

private val validUpdateRequestWithId = ApplicationUpdateDto(
    1,
    "job",
    "comp",
    null,
    null
)

private val validUpdateRequestWithNotExistingId = ApplicationUpdateDto(
    10,
    "job",
    "comp",
    null,
    null
)

private val validTransitionRequest = ApplicationTransitionRequestDto(1, "comment", null)
private val validTransitionRequestForTerminalApplication = ApplicationTransitionRequestDto(2, null, null)

private val invalidHiredWithScheduledWorkExperience = ApplicationTransitionRequestDto(
    3, null,
    ScheduledWorkExperienceDto(null, null, null, null, null)
)

private val validHiredWithScheduledWorkExperience = ApplicationTransitionRequestDto(
    3, null,
    ScheduledWorkExperienceDto("new job", "new place", "new company", LocalDate.now().plusMonths(1), "TBD")
)

class ApplicationServiceTest {
    private lateinit var capturedApplication: CapturingSlot<ApplicationEntity>
    private lateinit var capturedHistory: CapturingSlot<ApplicationHistoryEntity>

    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var applicationHistoryRepository: ApplicationHistoryRepository
    private lateinit var applicationValidationService: ApplicationValidationService
    private lateinit var applicationAccountService: ApplicantAccountService
    private lateinit var schedulerService: SchedulerService
    private lateinit var applicationService: ApplicationService

    @BeforeEach
    fun setup() {
        capturedApplication = slot()
        capturedHistory = slot()

        applicationRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(1)) } returns Optional.of(existingApplication)
            every { findById(eq(2)) } returns Optional.of(existingApplicationTwo)
            every { findById(eq(3)) } returns Optional.of(existingApplicationThree)

            every { save(capture(capturedApplication)) } returns mockk {}

            every { delete(any()) } just Runs
        }
        applicationHistoryRepository = mockk {
            every { save(capture(capturedHistory)) } returns mockk {}
        }
        applicationValidationService = mockk {
            every { validateUpdate(any()) } returns Result.failure(IllegalArgumentException())
            every { validateUpdate(eq(validUpdateRequestWithoutId)) } returns Result.success(Unit)
            every { validateUpdate(eq(validUpdateRequestWithId)) } returns Result.success(Unit)
            every { validateUpdate(eq(validUpdateRequestWithNotExistingId)) } returns Result.success(Unit)

            every { validateTransition(any()) } returns Result.failure(IllegalArgumentException())
            every { validateTransition(validTransitionRequest) } returns Result.success(Unit)
            every { validateTransition(validTransitionRequestForTerminalApplication) } returns Result.success(Unit)
            every { validateTransition(validHiredWithScheduledWorkExperience) } returns Result.success(Unit)
        }
        applicationAccountService = mockk {
            every { findById(any()) } returns Result.failure(IllegalArgumentException())
            every { findById(eq(1)) } returns Result.success(validAccount)
            every { findById(eq(2)) } returns Result.success(secondAccount)
        }
        schedulerService = mockk(relaxed = true)
        applicationService = ApplicationService(
            applicationRepository,
            applicationHistoryRepository,
            applicationValidationService,
            applicationAccountService,
            schedulerService
        )
    }

    @Test
    fun testSaveNewApplication() {
        val result = applicationService.save(1, validUpdateRequestWithoutId)

        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 1) { applicationRepository.save(any()) }
        assertNotNull(capturedApplication.captured)

        assertEquals(validUpdateRequestWithoutId.jobTitle, capturedApplication.captured.jobTitle)
        assertEquals(validUpdateRequestWithoutId.company, capturedApplication.captured.company)
        assertEquals(validUpdateRequestWithoutId.description, capturedApplication.captured.description)
        assertEquals(validUpdateRequestWithoutId.source, capturedApplication.captured.source)
        assertEquals(ApplicationStatus.UNSENT, capturedApplication.captured.status)
        assertNotNull(capturedApplication.captured.createdAt)
        assertNull(capturedApplication.captured.updatedAt)
        assertFalse { capturedApplication.captured.isArchived }
    }

    @Test
    fun testSaveNewApplicationWithNotExistingAccount() {
        val result = applicationService.save(3, validUpdateRequestWithoutId)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testSaveWithInvalidUpdate() {
        val result = applicationService.save(1, ApplicationUpdateDto(null, null, null, null, null))

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testSaveExistingApplication() {
        val result = applicationService.save(1, validUpdateRequestWithId)

        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 1) { applicationRepository.save(any()) }
        assertNotNull(capturedApplication.captured)
        assertEquals(validUpdateRequestWithoutId.jobTitle, capturedApplication.captured.jobTitle)
        assertEquals(validUpdateRequestWithoutId.company, capturedApplication.captured.company)
        assertEquals(validUpdateRequestWithoutId.description, capturedApplication.captured.description)
        assertEquals(validUpdateRequestWithoutId.source, capturedApplication.captured.source)
        assertEquals(ApplicationStatus.WAITING_FOR_FIRST_RESPONSE, capturedApplication.captured.status)
        assertEquals(existingApplication.createdAt, capturedApplication.captured.createdAt)
        assertNotNull(capturedApplication.captured.updatedAt)
        assertEquals(existingApplication.history, capturedApplication.captured.history)
        assertFalse { capturedApplication.captured.isArchived }
    }

    @Test
    fun testSaveExistingApplicationOfOtherAccount() {
        val result = applicationService.save(2, validUpdateRequestWithId)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testSaveNotExistingApplication() {
        val result = applicationService.save(1, validUpdateRequestWithNotExistingId)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testTransitionOfApplicationInTerminalStatus() {
        val result = applicationService.transition(
            2,
            ApplicationTransition.WITHDRAWN.id,
            validTransitionRequestForTerminalApplication
        )

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testTransitionOfApplication() {
        val result =
            applicationService.transition(1, ApplicationTransition.SCHEDULED_INTERVIEW.id, validTransitionRequest)

        assertTrue { result.isSuccess }
        verify(exactly = 1) { applicationHistoryRepository.save(any()) }
        verify(exactly = 1) { applicationRepository.save(any()) }
        assertNotNull(capturedHistory.captured)
        assertNotNull(capturedApplication.captured)

        assertEquals(ApplicationStatus.WAITING_FOR_FIRST_RESPONSE, capturedHistory.captured.source)
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, capturedHistory.captured.target)
        assertEquals(validTransitionRequest.comment, capturedHistory.captured.comment)

        assertEquals(existingApplication.id, capturedApplication.captured.id)
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, capturedApplication.captured.status)
        assertEquals(2, capturedApplication.captured.history.size)
        assertEquals(existingApplication.history[0], capturedApplication.captured.history[0])
        assertEquals(existingApplication.source, capturedApplication.captured.source)
        assertEquals(existingApplication.description, capturedApplication.captured.description)
        assertEquals(existingApplication.createdAt, capturedApplication.captured.createdAt)
        assertEquals(existingApplication.jobTitle, capturedApplication.captured.jobTitle)
        assertEquals(existingApplication.company, capturedApplication.captured.company)
        assertEquals(existingApplication.account, capturedApplication.captured.account)
        assertNotEquals(existingApplication.updatedAt, capturedApplication.captured.updatedAt)
        assertFalse { capturedApplication.captured.isArchived }
    }

    @Test
    fun testTransitionOfApplicationOfOtherAccount() {
        val result =
            applicationService.transition(2, ApplicationTransition.SCHEDULED_INTERVIEW.id, validTransitionRequest)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testTransitionWithNotExistingTransition() {
        val result = applicationService.transition(1, 100, validTransitionRequest)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testTransitionWithNotAllowedTransition() {
        val result = applicationService.transition(1, ApplicationTransition.SENT_APPLICATION.id, validTransitionRequest)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testTransitionWithInvalidScheduledWorkExperience() {
        val result = applicationService.transition(
            2,
            ApplicationTransition.OFFER_ACCEPTED.id,
            invalidHiredWithScheduledWorkExperience
        )

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationHistoryRepository.save(any()) }
        verify(exactly = 0) { applicationRepository.save(any()) }
        verify(exactly = 0) { schedulerService.scheduleWorkExperienceAddition(any()) }
    }

    @Test
    fun testTransitionWithScheduledWorkExperience() {
        val result = applicationService.transition(2,
            ApplicationTransition.OFFER_ACCEPTED.id,
            validHiredWithScheduledWorkExperience
        )

        assertTrue { result.isSuccess }
        verify(exactly = 1) { applicationHistoryRepository.save(any()) }
        verify(exactly = 1) { applicationRepository.save(any()) }
        verify(exactly = 1) { schedulerService.scheduleWorkExperienceAddition(any()) }
    }

    @Test
    fun testArchiveWithNotExistingApplication() {
        val result = applicationService.archive(1, 100)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testArchiveWithoutPermission() {
        val result = applicationService.archive(2, 1)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testArchiveOfOpenApplication() {
        val result = applicationService.archive(1, 1)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun testArchiveOfClosedApplication() {
        val result = applicationService.archive(2, 2)

        assertTrue { result.isSuccess }
        verify(exactly = 1) { applicationRepository.save(any()) }
        assertNotNull(capturedApplication.captured)
        assertEquals(existingApplicationTwo.id, capturedApplication.captured.id)
        assertEquals(existingApplicationTwo.jobTitle, capturedApplication.captured.jobTitle)
        assertEquals(existingApplicationTwo.company, capturedApplication.captured.company)
        assertEquals(existingApplicationTwo.createdAt, capturedApplication.captured.createdAt)
        assertEquals(existingApplicationTwo.updatedAt, capturedApplication.captured.updatedAt)
        assertEquals(existingApplicationTwo.status, capturedApplication.captured.status)
        assertEquals(existingApplicationTwo.source, capturedApplication.captured.source)
        assertEquals(existingApplicationTwo.description, capturedApplication.captured.description)
        assertEquals(existingApplicationTwo.account, capturedApplication.captured.account)
        assertEquals(existingApplicationTwo.history, capturedApplication.captured.history)
        assertTrue { capturedApplication.captured.isArchived }
    }

    @Test
    fun testDeleteWithNotExistingApplication() {
        val result = applicationService.delete(1, 100)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationRepository.delete(any()) }
    }

    @Test
    fun testDeleteWithUnauthorizedAccount() {
        val result = applicationService.delete(2, 1)

        assertTrue { result.isFailure }
        verify(exactly = 0) { applicationRepository.delete(any()) }
    }

    @Test
    fun testDelete() {
        val result = applicationService.delete(1, 1)

        assertTrue { result.isSuccess }
        verify(exactly = 1) { applicationRepository.delete(any()) }
    }
}
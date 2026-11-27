package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class ApplicationSearchSortingTest(
    @Autowired private val service: ApplicationService,
    @Autowired private val repository: ApplicationRepository,
    @Autowired private val entityManager: EntityManager
) {
    @Test
    fun `sorts by relevance then activity date across pages`() {
        val account = createAccount()
        val olderDraft = save(account, "Older draft", ApplicationStatus.UNSENT, updatedDay = 2)
        val newDraft = save(account, "New draft", ApplicationStatus.UNSENT, createdDay = 3)
        val interview = save(account, "Interview", ApplicationStatus.INTERVIEW_SCHEDULED, updatedDay = 4)
        val offer = save(account, "Offer", ApplicationStatus.OFFER_RECEIVED, updatedDay = 4)
        val awaitingResponse = save(account, "Awaiting response", ApplicationStatus.WAITING_FOR_FIRST_RESPONSE, updatedDay = 5)
        val awaitingFeedback = save(account, "Awaiting feedback", ApplicationStatus.WAITING_FOR_INTERVIEW_FEEDBACK, updatedDay = 6)
        val rejected = save(account, "Rejected", ApplicationStatus.REJECTED, updatedDay = 7)
        val archived = save(account, "Archived", ApplicationStatus.WITHDRAWN, updatedDay = 8, archived = true)

        val request = ApplicationSearchRequest(0, 3, null, null, true, null, null)
        val pages = (0..2).map { page ->
            service.searchApplications(account.id!!, request.copy(page = page)).getOrThrow()
        }

        assertEquals(listOf(8L, 8L, 8L), pages.map { it.totalElements })
        assertEquals(
            listOf(newDraft, olderDraft, offer, interview, awaitingFeedback, awaitingResponse, archived, rejected)
                .map { it.id },
            pages.flatMap { page -> page.content.map { it.id } }
        )
    }

    @Test
    fun `uses the requested column and direction instead of relevance when explicitly sorted`() {
        val account = createAccount()
        val rejected = save(account, "Alpha", ApplicationStatus.REJECTED)
        val draft = save(account, "Zulu", ApplicationStatus.UNSENT)
        val request = ApplicationSearchRequest(0, 10, null, null, false, "jobTitle", "ascending")

        val ascending = service.searchApplications(account.id!!, request).getOrThrow()
        val descending = service.searchApplications(
            account.id!!,
            request.copy(sortDirection = "descending")
        ).getOrThrow()

        assertEquals(listOf(rejected.id, draft.id), ascending.content.map { it.id })
        assertEquals(listOf(draft.id, rejected.id), descending.content.map { it.id })
    }

    private fun createAccount(): ApplicantAccountEntity =
        ApplicantAccountEntity("applicant", password = null, isOAuthUser = false, isVerified = true)
            .also { entityManager.persist(it) }

    private fun save(
        account: ApplicantAccountEntity,
        title: String,
        status: ApplicationStatus,
        createdDay: Int = 1,
        updatedDay: Int? = null,
        archived: Boolean = false
    ): ApplicationEntity = repository.save(
        ApplicationEntity(
            jobTitle = title,
            company = "Company",
            status = status,
            createdAt = LocalDateTime.of(2026, 1, createdDay, 0, 0),
            updatedAt = updatedDay?.let { LocalDateTime.of(2026, 1, it, 0, 0) },
            isArchived = archived,
            account = account
        )
    )
}

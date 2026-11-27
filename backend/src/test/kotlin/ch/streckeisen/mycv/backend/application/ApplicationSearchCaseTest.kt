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
import org.springframework.data.domain.PageRequest

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class ApplicationSearchCaseTest(
    @Autowired private val repository: ApplicationRepository,
    @Autowired private val entityManager: EntityManager
) {
    @Test
    fun `matches partial job titles and company names regardless of case`() {
        val account = createAccount("applicant")
        val otherAccount = createAccount("other")
        val titleMatch = saveApplication(account, "Senior Engineer", "First Company")
        val companyMatch = saveApplication(account, "Analyst", "Engineering Studio")
        saveApplication(account, "Designer", "Another Company")
        saveApplication(account, "Archived Engineer", "First Company", archived = true)
        saveApplication(otherAccount, "Engineer", "First Company")

        for (term in listOf("engin", "ENGIN", "eNgIn")) {
            val results = repository.searchByAccountId(
                account.id!!,
                term,
                pageable = PageRequest.of(0, 10)
            )
            assertEquals(setOf(titleMatch.id, companyMatch.id), results.content.map { it.id }.toSet(), term)
        }

        val unfiltered = repository.searchByAccountId(account.id!!, pageable = PageRequest.of(0, 10))
        assertEquals(3L, unfiltered.totalElements)
    }

    private fun createAccount(username: String): ApplicantAccountEntity =
        ApplicantAccountEntity(username, password = null, isOAuthUser = false, isVerified = true)
            .also { entityManager.persist(it) }

    private fun saveApplication(
        account: ApplicantAccountEntity,
        title: String,
        company: String,
        archived: Boolean = false
    ): ApplicationEntity = repository.save(
        ApplicationEntity(
            jobTitle = title,
            company = company,
            status = if (archived) ApplicationStatus.REJECTED else ApplicationStatus.UNSENT,
            createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
            isArchived = archived,
            account = account
        )
    )
}

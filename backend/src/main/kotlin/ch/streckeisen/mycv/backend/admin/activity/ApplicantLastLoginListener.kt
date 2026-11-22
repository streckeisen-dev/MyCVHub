package ch.streckeisen.mycv.backend.admin.activity

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class ApplicantLastLoginListener(private val applicantAccountRepository: ApplicantAccountRepository) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun recordLastLogin(event: UserActivityApplicationEvent) {
        if (event.eventType != ActivityEventType.LOGIN) return
        runCatching {
            applicantAccountRepository.updateLastLoginAt(event.applicantAccountId, event.occurredAt)
        }.onFailure { logger.error(it) { "Failed to update applicant last login" } }
    }
}

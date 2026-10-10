package ch.streckeisen.mycv.backend.admin.activity

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class UserActivityEventListener(
    private val userActivityEventRepository: UserActivityEventRepository
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun recordUserActivity(event: UserActivityApplicationEvent) {
        runCatching {
            userActivityEventRepository.save(
                UserActivityEventEntity(
                    applicantAccountId = event.applicantAccountId,
                    eventType = event.eventType,
                    timestamp = event.occurredAt
                )
            )
        }.onFailure {
            logger.error(it) {
                "Failed to record ${event.eventType} activity event for account ${event.applicantAccountId}"
            }
        }
    }
}

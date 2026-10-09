package ch.streckeisen.mycv.backend.admin.activity

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class UserActivityEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun publish(accountId: Long, eventType: ActivityEventType) {
        applicationEventPublisher.publishEvent(UserActivityApplicationEvent(accountId, eventType))
    }
}

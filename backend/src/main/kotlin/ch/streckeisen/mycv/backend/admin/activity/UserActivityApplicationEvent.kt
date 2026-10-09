package ch.streckeisen.mycv.backend.admin.activity

import java.time.LocalDateTime

data class UserActivityApplicationEvent(
    val applicantAccountId: Long,
    val eventType: ActivityEventType,
    val occurredAt: LocalDateTime = LocalDateTime.now()
)

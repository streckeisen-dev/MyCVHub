package ch.streckeisen.mycv.backend.admin.activity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class UserActivityEventEntity(
    var applicantAccountId: Long,
    @Enumerated(EnumType.STRING)
    var eventType: ActivityEventType,
    var timestamp: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)

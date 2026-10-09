package ch.streckeisen.mycv.backend.admin.audit

import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class AdminAuditLogEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    var admin: AdminAccountEntity?,
    var actorSource: String,
    var action: String,
    var result: String,
    var targetType: String?,
    var targetId: String?,
    var timestamp: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)

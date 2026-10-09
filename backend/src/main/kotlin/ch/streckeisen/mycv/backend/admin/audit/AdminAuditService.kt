package ch.streckeisen.mycv.backend.admin.audit

import ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

const val ADMIN_AUDIT_SOURCE_SYSTEM_BOOTSTRAP = "SYSTEM_BOOTSTRAP"
const val ADMIN_AUDIT_SOURCE_ADMIN = "ADMIN"
const val ADMIN_AUDIT_RESULT_SUCCESS = "SUCCESS"
const val ADMIN_AUDIT_RESULT_FAILURE = "FAILURE"

@Service
class AdminAuditService(
    private val adminAuditLogRepository: AdminAuditLogRepository
) {
    fun record(
        admin: AdminAccountEntity?,
        actorSource: String,
        action: String,
        result: String,
        targetType: String? = null,
        targetId: String? = null
    ) {
        runCatching {
            adminAuditLogRepository.save(
                AdminAuditLogEntity(
                    admin = admin,
                    actorSource = actorSource,
                    action = action,
                    result = result,
                    targetType = targetType,
                    targetId = targetId
                )
            )
        }.onFailure {
            logger.error(it) { "Failed to record admin audit event $action for target $targetId" }
        }
    }
}

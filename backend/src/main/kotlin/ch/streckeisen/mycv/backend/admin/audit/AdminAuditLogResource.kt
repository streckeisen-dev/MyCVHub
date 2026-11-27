package ch.streckeisen.mycv.backend.admin.audit

import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.admin.toAdminOffsetDateTime
import ch.streckeisen.mycv.backend.security.annotations.RequiresAdminRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

data class AdminAuditLogDto(
    val id: Long,
    val timestamp: OffsetDateTime,
    val actor: String?,
    val actorSource: String,
    val action: String,
    val result: String,
    val targetType: String?,
    val targetId: String?
)

@RestController
@RequestMapping("/api/admin/audit-log")
@RequiresAdminRole(role = AdminRole.ADMIN, exact = true)
class AdminAuditLogResource(private val repository: AdminAuditLogRepository) {
    @GetMapping
    @Transactional(readOnly = true)
    fun list(@RequestParam(defaultValue = "0") page: Int): Page<AdminAuditLogDto> =
        repository.findAuditPage(PageRequest.of(page.coerceAtLeast(0), 25)).map {
            AdminAuditLogDto(it.id!!, it.timestamp.toAdminOffsetDateTime(), it.actorUsername ?: it.admin?.username, it.actorSource, it.action, it.result, it.targetType, it.targetId)
        }
}

package ch.streckeisen.mycv.backend.admin.audit

import org.springframework.data.repository.CrudRepository

interface AdminAuditLogRepository : CrudRepository<AdminAuditLogEntity, Long>

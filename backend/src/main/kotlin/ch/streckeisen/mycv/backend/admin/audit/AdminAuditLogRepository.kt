package ch.streckeisen.mycv.backend.admin.audit

import org.springframework.data.repository.CrudRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AdminAuditLogRepository : CrudRepository<AdminAuditLogEntity, Long> {
    @Query(value = "SELECT log FROM AdminAuditLogEntity log LEFT JOIN FETCH log.admin ORDER BY log.timestamp DESC, log.id DESC", countQuery = "SELECT COUNT(log) FROM AdminAuditLogEntity log")
    fun findAuditPage(pageable: Pageable): Page<AdminAuditLogEntity>

    @Modifying
    @Query("UPDATE AdminAuditLogEntity log SET log.admin = null WHERE log.admin.id = :adminId")
    fun detachDeletedAdmin(adminId: Long): Int
}

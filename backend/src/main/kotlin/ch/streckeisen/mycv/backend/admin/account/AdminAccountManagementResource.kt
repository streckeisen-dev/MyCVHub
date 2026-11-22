package ch.streckeisen.mycv.backend.admin.account

import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_RESULT_SUCCESS
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_SOURCE_ADMIN
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditService
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditLogRepository
import ch.streckeisen.mycv.backend.admin.toAdminOffsetDateTime
import ch.streckeisen.mycv.backend.admin.auth.getAdminPrincipal
import ch.streckeisen.mycv.backend.admin.bootstrap.AdminBootstrapProperties
import ch.streckeisen.mycv.backend.admin.bootstrap.AdminTemporaryPasswordGenerator
import ch.streckeisen.mycv.backend.security.annotations.RequiresAdminRole
import org.springframework.http.HttpStatus
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class AdminAccountSummary(val id: Long, val username: String, val active: Boolean, val createdAt: OffsetDateTime, val lastLoginAt: OffsetDateTime?)
data class CreateAdminRequest(val username: String)
data class CreatedAdminResponse(val username: String, val temporaryPassword: String, val expiresAt: OffsetDateTime)

@RestController
@RequestMapping("/api/admin/accounts")
@RequiresAdminRole(role = AdminRole.SUPER_ADMIN, exact = true)
class AdminAccountManagementResource(
    private val repository: AdminAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordGenerator: AdminTemporaryPasswordGenerator,
    private val properties: AdminBootstrapProperties,
    private val audit: AdminAuditService,
    private val auditLogRepository: AdminAuditLogRepository
) {
    @GetMapping
    fun list(): List<AdminAccountSummary> = repository.findAll().filter { it.role == AdminRole.ADMIN }
        .sortedBy { it.username.lowercase() }
        .map { AdminAccountSummary(it.id!!, it.username, it.isActive, it.createdAt.toAdminOffsetDateTime(), it.lastLoginAt?.toAdminOffsetDateTime()) }

    @PostMapping
    @Transactional
    fun create(@RequestBody request: CreateAdminRequest): ResponseEntity<CreatedAdminResponse> {
        val username = request.username.trim()
        if (username.isEmpty() || username.length > 100) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username")
        if (repository.findByUsername(username).isPresent) throw ResponseStatusException(HttpStatus.CONFLICT, "Admin already exists")
        val password = passwordGenerator.generate()
        val expiresAt = LocalDateTime.now().plusMinutes(properties.temporaryPasswordExpirationMinutes)
        val encodedPassword = passwordEncoder.encode(password) ?: throw IllegalStateException("Password encoding failed")
        val account = repository.save(AdminAccountEntity(username = username, password = encodedPassword, role = AdminRole.ADMIN, isActive = true, mustChangePassword = true, temporaryPasswordExpiresAt = expiresAt, passwordChangedAt = LocalDateTime.now()))
        record("ADMIN_CREATE", account)
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(CreatedAdminResponse(username, password, expiresAt.toAdminOffsetDateTime()))
    }

    @PostMapping("/{id}/reset-password")
    @Transactional
    fun resetPassword(@PathVariable id: Long): ResponseEntity<CreatedAdminResponse> {
        val account = repository.findByIdForUpdate(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (account.role != AdminRole.ADMIN) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        if (!account.isActive) throw ResponseStatusException(HttpStatus.CONFLICT, "Admin is inactive")
        val password = passwordGenerator.generate()
        val expiresAt = LocalDateTime.now().plusMinutes(properties.temporaryPasswordExpirationMinutes)
        account.password = passwordEncoder.encode(password) ?: throw IllegalStateException("Password encoding failed")
        account.passwordChangedAt = LocalDateTime.now()
        account.mustChangePassword = true
        account.temporaryPasswordExpiresAt = expiresAt
        repository.save(account)
        record("ADMIN_RESET_PASSWORD", account)
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(CreatedAdminResponse(account.username, password, expiresAt.toAdminOffsetDateTime()))
    }

    @PostMapping("/{id}/deactivate")
    @Transactional
    fun deactivate(@PathVariable id: Long) {
        val account = repository.findByIdForUpdate(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (account.role != AdminRole.ADMIN) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        account.isActive = false
        repository.save(account)
        record("ADMIN_DEACTIVATE", account)
    }

    @DeleteMapping("/{id}")
    @Transactional
    fun delete(@PathVariable id: Long) {
        val account = repository.findByIdForUpdate(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (account.role != AdminRole.ADMIN) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        record("ADMIN_DELETE", account)
        auditLogRepository.detachDeletedAdmin(id)
        repository.delete(account)
    }

    private fun record(action: String, target: AdminAccountEntity) {
        val actorId = SecurityContextHolder.getContext().getAdminPrincipal().id
        val actor = repository.findById(actorId).orElseThrow()
        audit.record(actor, ADMIN_AUDIT_SOURCE_ADMIN, action, ADMIN_AUDIT_RESULT_SUCCESS, "ADMIN_ACCOUNT", target.id.toString())
    }
}

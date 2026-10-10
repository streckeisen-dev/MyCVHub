package ch.streckeisen.mycv.backend.admin.bootstrap

import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import ch.streckeisen.mycv.backend.admin.account.AdminRole
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_RESULT_FAILURE
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_RESULT_SUCCESS
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_SOURCE_SYSTEM_BOOTSTRAP
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

private const val ADMIN_BOOTSTRAP_ACTION = "ADMIN_BOOTSTRAP_RESET"
private const val ADMIN_ACCOUNT_TARGET = "ADMIN_ACCOUNT"

@Service
class AdminBootstrapService(
    private val adminAccountRepository: AdminAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val temporaryPasswordGenerator: AdminTemporaryPasswordGenerator,
    private val adminAuditService: AdminAuditService,
    private val adminBootstrapProperties: AdminBootstrapProperties
) {
    @Transactional
    fun bootstrap(username: String, resetExisting: Boolean): AdminBootstrapResult {
        val admin = adminAccountRepository.findByUsername(username)
            .getOrElse {
                adminAuditService.record(
                    admin = null,
                    actorSource = ADMIN_AUDIT_SOURCE_SYSTEM_BOOTSTRAP,
                    action = ADMIN_BOOTSTRAP_ACTION,
                    result = ADMIN_AUDIT_RESULT_FAILURE,
                    targetType = ADMIN_ACCOUNT_TARGET,
                    targetId = username
                )
                throw IllegalArgumentException("No admin account exists for username $username")
            }

        if (admin.role != AdminRole.SUPER_ADMIN) {
            adminAuditService.record(
                admin = admin,
                actorSource = ADMIN_AUDIT_SOURCE_SYSTEM_BOOTSTRAP,
                action = ADMIN_BOOTSTRAP_ACTION,
                result = ADMIN_AUDIT_RESULT_FAILURE,
                targetType = ADMIN_ACCOUNT_TARGET,
                targetId = admin.id?.toString()
            )
            throw IllegalStateException("Only SUPER_ADMIN accounts can be bootstrapped")
        }

        val isInitialBootstrapState = !admin.isActive || admin.mustChangePassword
        if (!isInitialBootstrapState && !resetExisting) {
            adminAuditService.record(
                admin = admin,
                actorSource = ADMIN_AUDIT_SOURCE_SYSTEM_BOOTSTRAP,
                action = ADMIN_BOOTSTRAP_ACTION,
                result = ADMIN_AUDIT_RESULT_FAILURE,
                targetType = ADMIN_ACCOUNT_TARGET,
                targetId = admin.id?.toString()
            )
            throw IllegalStateException("Refusing to reset active admin without resetExisting=true")
        }

        val temporaryPassword = temporaryPasswordGenerator.generate()
        val expiresAt = LocalDateTime.now().plusMinutes(adminBootstrapProperties.temporaryPasswordExpirationMinutes)
        admin.password = passwordEncoder.encode(temporaryPassword)
            ?: throw IllegalStateException("Failed to encode temporary password")
        admin.isActive = true
        admin.mustChangePassword = true
        admin.temporaryPasswordExpiresAt = expiresAt
        admin.passwordChangedAt = LocalDateTime.now()
        adminAccountRepository.save(admin)

        adminAuditService.record(
            admin = admin,
            actorSource = ADMIN_AUDIT_SOURCE_SYSTEM_BOOTSTRAP,
            action = ADMIN_BOOTSTRAP_ACTION,
            result = ADMIN_AUDIT_RESULT_SUCCESS,
            targetType = ADMIN_ACCOUNT_TARGET,
            targetId = admin.id?.toString()
        )

        return AdminBootstrapResult(admin.username, temporaryPassword, expiresAt)
    }
}

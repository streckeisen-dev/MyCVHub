package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.account.PASSWORD_MAX_LENGTH
import ch.streckeisen.mycv.backend.account.auth.AuthenticationValidationService
import ch.streckeisen.mycv.backend.admin.account.AdminAccountRepository
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_RESULT_FAILURE
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_RESULT_SUCCESS
import ch.streckeisen.mycv.backend.admin.audit.ADMIN_AUDIT_SOURCE_ADMIN
import ch.streckeisen.mycv.backend.admin.audit.AdminAuditService
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

private const val ADMIN_LOGIN_ACTION = "ADMIN_LOGIN"
private const val ADMIN_CHANGE_PASSWORD_ACTION = "ADMIN_CHANGE_PASSWORD"
private const val ADMIN_ACCOUNT_TARGET = "ADMIN_ACCOUNT"

@Service
class AdminAuthenticationService(
    private val adminAccountRepository: AdminAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val adminAuthTokenService: AdminAuthTokenService,
    private val authenticationValidationService: AuthenticationValidationService,
    private val adminAuditService: AdminAuditService,
    private val adminLoginRateLimiter: AdminLoginRateLimiter
) {
    @Transactional
    fun authenticate(loginRequest: AdminLoginRequestDto): Result<AdminAuthTokens> {
        val username = loginRequest.username?.trim()
        val password = loginRequest.password
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return Result.failure(BadCredentialsException("Invalid admin credentials"))
        }
        val rateLimitKey = username.lowercase()

        if (adminLoginRateLimiter.isLocked(rateLimitKey)) {
            adminAuditService.record(
                admin = null,
                actorSource = ADMIN_AUDIT_SOURCE_ADMIN,
                action = ADMIN_LOGIN_ACTION,
                result = ADMIN_AUDIT_RESULT_FAILURE,
                targetType = ADMIN_ACCOUNT_TARGET,
                targetId = username
            )
            return Result.failure(BadCredentialsException("Invalid admin credentials"))
        }

        val admin = adminAccountRepository.findByUsername(username)
            .getOrElse {
                adminAuditService.record(
                    admin = null,
                    actorSource = ADMIN_AUDIT_SOURCE_ADMIN,
                    action = ADMIN_LOGIN_ACTION,
                    result = ADMIN_AUDIT_RESULT_FAILURE,
                        targetType = ADMIN_ACCOUNT_TARGET,
                        targetId = username
                    )
                    adminLoginRateLimiter.recordFailure(rateLimitKey)
                    return Result.failure(BadCredentialsException("Invalid admin credentials"))
                }

        val temporaryPasswordExpired = admin.temporaryPasswordExpiresAt?.isBefore(LocalDateTime.now()) == true
        if (!admin.isActive || temporaryPasswordExpired || !passwordEncoder.matches(password, admin.password)) {
            adminAuditService.record(
                admin = admin,
                actorSource = ADMIN_AUDIT_SOURCE_ADMIN,
                action = ADMIN_LOGIN_ACTION,
                result = ADMIN_AUDIT_RESULT_FAILURE,
                targetType = ADMIN_ACCOUNT_TARGET,
                targetId = admin.id?.toString()
            )
            adminLoginRateLimiter.recordFailure(rateLimitKey)
            return Result.failure(BadCredentialsException("Invalid admin credentials"))
        }

        adminLoginRateLimiter.recordSuccess(rateLimitKey)
        admin.lastLoginAt = LocalDateTime.now()
        adminAccountRepository.save(admin)
        adminAuditService.record(
            admin = admin,
            actorSource = ADMIN_AUDIT_SOURCE_ADMIN,
            action = ADMIN_LOGIN_ACTION,
            result = ADMIN_AUDIT_RESULT_SUCCESS,
            targetType = ADMIN_ACCOUNT_TARGET,
            targetId = admin.id?.toString()
        )

        return adminAuthTokenService.generateAuthData(admin.username)
    }

    fun refreshAccessToken(oldRefreshToken: String): Result<AdminAuthTokens> {
        val username = adminAuthTokenService.validateRefreshToken(oldRefreshToken)
            .getOrElse { return Result.failure(it) }
        return adminAuthTokenService.generateAuthData(username)
    }

    @Transactional
    fun changePassword(adminId: Long, changePasswordDto: AdminChangePasswordDto): Result<AdminAuthTokens> {
        val admin = adminAccountRepository.findById(adminId)
            .getOrElse { return Result.failure(IllegalArgumentException("Admin not found")) }

        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()
        authenticationValidationService.validatePassword(
            changePasswordDto.password,
            changePasswordDto.confirmPassword,
            validationErrorBuilder
        )
        if (validationErrorBuilder.hasErrors()) {
            adminAuditService.record(
                admin = admin,
                actorSource = ADMIN_AUDIT_SOURCE_ADMIN,
                action = ADMIN_CHANGE_PASSWORD_ACTION,
                result = ADMIN_AUDIT_RESULT_FAILURE,
                targetType = ADMIN_ACCOUNT_TARGET,
                targetId = admin.id?.toString()
            )
            return Result.failure(validationErrorBuilder.build("Invalid password"))
        }

        val newPassword = changePasswordDto.password
            ?: return Result.failure(IllegalArgumentException("Password is required"))
        val encodedPassword = passwordEncoder.encode(newPassword)
            ?: return Result.failure(IllegalArgumentException("Password encoding failed"))
        if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
            return Result.failure(IllegalArgumentException("Encoded password is too long"))
        }
        admin.password = encodedPassword
        admin.mustChangePassword = false
        admin.temporaryPasswordExpiresAt = null
        admin.passwordChangedAt = LocalDateTime.now()
        adminAccountRepository.save(admin)
        adminAuditService.record(
            admin = admin,
            actorSource = ADMIN_AUDIT_SOURCE_ADMIN,
            action = ADMIN_CHANGE_PASSWORD_ACTION,
            result = ADMIN_AUDIT_RESULT_SUCCESS,
            targetType = ADMIN_ACCOUNT_TARGET,
            targetId = admin.id?.toString()
        )
        return adminAuthTokenService.generateAuthData(admin.username)
    }
}

package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.admin.auth.AdminPrincipal
import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import ch.streckeisen.mycv.backend.security.annotations.RequiresAdminRole
import ch.streckeisen.mycv.backend.security.annotations.RequiresAccountStatus
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.rememberme.InvalidCookieException
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Aspect
@Component
class EndpointSecurityAspect {
    @Before(
        """(
            @annotation(org.springframework.web.bind.annotation.RequestMapping) ||
            @annotation(org.springframework.web.bind.annotation.GetMapping) || 
            @annotation(org.springframework.web.bind.annotation.PostMapping) ||
            @annotation(org.springframework.web.bind.annotation.PutMapping) ||
            @annotation(org.springframework.web.bind.annotation.DeleteMapping)
           ) && 
            execution(* ch.streckeisen.mycv.backend..*(..))            
        """

    )
    fun authorize(joinPoint: JoinPoint) {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        if (findAnnotation<PublicApi>(method) != null) {
            return
        }

        val authentication = requireAuthentication()
        val principal = authentication.principal
        val requiresAdminRoleAnnotation = findAnnotation<RequiresAdminRole>(method)
        if (requiresAdminRoleAnnotation != null) {
            authorizeAdmin(principal, requiresAdminRoleAnnotation)
            return
        }

        authorizeApplicant(principal, findAnnotation<RequiresAccountStatus>(method))
    }

    private fun requireAuthentication(): Authentication {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw InvalidCookieException("Unauthorized")
        }
        return authentication
    }

    private fun authorizeAdmin(principal: Any?, annotation: RequiresAdminRole) {
        if (principal !is AdminPrincipal) {
            throw AccessDeniedException("Access denied: Admin account required")
        }
        if (principal.mustChangePassword && !annotation.allowMustChangePassword) {
            throw AccessDeniedException("Access denied: Admin password change required")
        }

        val requiredRole = annotation.role
        val hasRequiredRole = if (annotation.exact) {
            principal.role == requiredRole
        } else {
            principal.role.permissionValue >= requiredRole.permissionValue
        }
        if (!hasRequiredRole) {
            throw AccessDeniedException("Access denied: Admin role does not fulfill requirement ${requiredRole.name}")
        }
    }

    private fun authorizeApplicant(principal: Any?, annotation: RequiresAccountStatus?) {
        if (principal !is MyCvPrincipal) {
            throw AccessDeniedException("Access denied: Applicant account required")
        }
        val userAccountStatus = principal.status

        if (annotation != null) {
            authorizeApplicantStatus(userAccountStatus, annotation)
            return
        }

        if (userAccountStatus != AccountStatus.VERIFIED) {
            throw AccessDeniedException("Access denied")
        }
    }

    private fun authorizeApplicantStatus(userAccountStatus: AccountStatus, annotation: RequiresAccountStatus) {
        val requiredStatus = annotation.accountStatus
        val hasRequiredStatus = if (annotation.exact) {
            userAccountStatus == requiredStatus
        } else {
            userAccountStatus.permissionValue >= requiredStatus.permissionValue
        }
        if (!hasRequiredStatus) {
            throw AccessDeniedException("Access denied: Account does not fulfill status requirement ${requiredStatus.name}")
        }
    }

    private inline fun <reified T : Annotation> findAnnotation(method: Method): T? {
        return method.annotations.find { it is T } as T?
            ?: method.declaringClass.annotations.find { it is T } as T?
    }
}

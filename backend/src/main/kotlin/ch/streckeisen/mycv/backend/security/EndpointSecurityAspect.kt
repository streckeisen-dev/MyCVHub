package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import ch.streckeisen.mycv.backend.security.annotations.RequiresAccountStatus
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.rememberme.InvalidCookieException
import org.springframework.stereotype.Component

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
        val isPublicApiMethod = methodSignature.method.annotations.any { it is PublicApi }
        val isPublicApiClass = methodSignature.method.declaringClass.annotations.any { it is PublicApi }
        if (isPublicApiMethod || isPublicApiClass) {
            return
        }

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw InvalidCookieException("Unauthorized")
        }
        val userAccountStatus = authentication.getMyCvPrincipal().status

        val methodRequiresAccountStatusAnnotation =
            methodSignature.method.annotations.find { it is RequiresAccountStatus } as RequiresAccountStatus?
        val classRequiresAccountStatusAnnotation =
            methodSignature.method.declaringClass.annotations.find { it is RequiresAccountStatus } as RequiresAccountStatus?
        val requiresAccountStatusAnnotation =
            methodRequiresAccountStatusAnnotation ?: classRequiresAccountStatusAnnotation
        if (requiresAccountStatusAnnotation != null) {
            val requiredStatus = requiresAccountStatusAnnotation.accountStatus
            if (requiresAccountStatusAnnotation.exact) {
                if (userAccountStatus == requiredStatus) {
                    return
                }
                throw AccessDeniedException("Access denied: Account does not fulfill status requirement ${requiredStatus.name}")
            }

            if (userAccountStatus.permissionValue >= requiredStatus.permissionValue) {
                return
            }
            throw AccessDeniedException("Access denied: Account does not fulfill status requirement ${requiredStatus.name}")
        }

        if (userAccountStatus != AccountStatus.VERIFIED) {
            throw AccessDeniedException("Access denied")
        }
    }
}
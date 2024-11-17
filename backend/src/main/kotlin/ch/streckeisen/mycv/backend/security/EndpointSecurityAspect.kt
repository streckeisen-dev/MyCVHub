package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.AccountStatus
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder
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
            !@annotation(ch.streckeisen.mycv.backend.security.PublicApi) &&
            execution(* ch.streckeisen.mycv.backend..*(..))            
        """

    )
    fun authorize(joinPoint: JoinPoint) {
        val methodSignature = joinPoint.signature as MethodSignature
        val isPublicApiClass = methodSignature.method.declaringClass.annotations.any { it is PublicApi }
        if (isPublicApiClass) {
            return
        }

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw BadCredentialsException("Unauthorized")
        }
        val userAccountStatus = authentication.getMyCvPrincipal().status

        val methodRequiresAccountStatusAnnotation =
            methodSignature.method.annotations.find { it is RequiresAccountStatus } as RequiresAccountStatus?
        val classRequiresAccountStatusAnnotation =
            methodSignature.declaringType.annotations.find { it is RequiresAccountStatus } as RequiresAccountStatus?
        val requiresAccountStatusAnnotation =
            methodRequiresAccountStatusAnnotation ?: classRequiresAccountStatusAnnotation
        if (requiresAccountStatusAnnotation != null) {
            val requiredStatus = requiresAccountStatusAnnotation.accountStatus
            if (requiresAccountStatusAnnotation.exact && userAccountStatus == requiredStatus) {
                return
            }
            if (userAccountStatus.permissionValue >= requiredStatus.permissionValue) {
                return
            } else {
                throw AccessDeniedException("Access denied: Account does not fulfill status requirement ${requiredStatus.name}")
            }
        }

        if (userAccountStatus != AccountStatus.VERIFIED) {
            throw AccessDeniedException("Access denied")
        }
    }
}
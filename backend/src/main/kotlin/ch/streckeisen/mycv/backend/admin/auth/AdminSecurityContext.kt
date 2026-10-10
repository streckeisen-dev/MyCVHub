package ch.streckeisen.mycv.backend.admin.auth

import org.springframework.security.core.context.SecurityContext

fun SecurityContext.getAdminPrincipal(): AdminPrincipal {
    return authentication!!.principal as AdminPrincipal
}

fun SecurityContext.getAdminPrincipalOrNull(): AdminPrincipal? {
    return authentication?.principal as? AdminPrincipal
}

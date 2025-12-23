package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext

private const val ACCESS_DENIED_ERROR_KEY = "${MYCV_KEY_PREFIX}.auth.accessDenied"

fun SecurityContext.getMyCvPrincipal(): MyCvPrincipal {
    if (authentication is UsernamePasswordAuthenticationToken) {
        return (authentication?.principal ?: throw LocalizedException(ACCESS_DENIED_ERROR_KEY)) as MyCvPrincipal
    }
    throw LocalizedException(ACCESS_DENIED_ERROR_KEY)
}

fun SecurityContext.getMyCvPrincipalOrNull(): MyCvPrincipal? {
    if (authentication is UsernamePasswordAuthenticationToken) {
        return authentication?.principal as MyCvPrincipal
    }
    return null
}
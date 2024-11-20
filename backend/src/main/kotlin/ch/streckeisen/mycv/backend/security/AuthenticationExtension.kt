package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

fun Authentication.getMyCvPrincipal(): MyCvPrincipal {
    if (this is UsernamePasswordAuthenticationToken) {
        return principal as MyCvPrincipal
    }
    throw LocalizedException("${MYCV_KEY_PREFIX}.auth.accessDenied")
}

fun Authentication.getMyCvPrincipalOrNull(): MyCvPrincipal? {
    if (this is UsernamePasswordAuthenticationToken) {
        return principal as MyCvPrincipal
    }
    return null
}
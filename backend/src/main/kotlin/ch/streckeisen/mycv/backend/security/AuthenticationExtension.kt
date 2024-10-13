package ch.streckeisen.mycv.backend.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

fun Authentication.getMyCvPrincipal(): MyCvPrincipal {
    if (this is UsernamePasswordAuthenticationToken) {
        return principal as MyCvPrincipal
    }
    throw AccessDeniedException("You are not authorized to perform this operation")
}

fun Authentication.getMyCvPrincipalOrNull(): MyCvPrincipal? {
    if (this is UsernamePasswordAuthenticationToken) {
        return principal as MyCvPrincipal
    }
    return null
}
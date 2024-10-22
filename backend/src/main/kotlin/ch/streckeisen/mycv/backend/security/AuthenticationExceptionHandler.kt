package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

private const val UNAUTHORIZED_ERROR_KEY = "${MYCV_KEY_PREFIX}.exceptions.unauthorized"

class AuthenticationExceptionHandler(
    private val messagesService: MessagesService
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messagesService.getMessage(UNAUTHORIZED_ERROR_KEY))
    }
}
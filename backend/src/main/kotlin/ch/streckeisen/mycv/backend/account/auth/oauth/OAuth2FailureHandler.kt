package ch.streckeisen.mycv.backend.account.auth.oauth

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private const val OAUTH_FAILURE_REDIRECT = "login/oauth-failure"

@Component
class OAuth2FailureHandler(
    @param:Value(value = $$"${my-cv.frontend.base-url}")
    private val frontendBaseUrl: String,
) : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        logger.error(exception) { "OAuth2 authentication failed" }
        response.sendRedirect("${frontendBaseUrl}/$OAUTH_FAILURE_REDIRECT")
    }
}
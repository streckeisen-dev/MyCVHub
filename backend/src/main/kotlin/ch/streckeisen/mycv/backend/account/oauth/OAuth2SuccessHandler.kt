package ch.streckeisen.mycv.backend.account.oauth

import ch.streckeisen.mycv.backend.account.auth.AuthenticationService
import ch.streckeisen.mycv.backend.account.auth.createAccessCookie
import ch.streckeisen.mycv.backend.account.auth.createRefreshCookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val authenticationService: AuthenticationService,
    @Value("\${frontend.base-url}")
    private val frontendBaseUrl: String,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauthUser = authentication.principal as OAuth2User
        val registrationId = (authentication as OAuth2AuthenticationToken).authorizedClientRegistrationId

        val authDataResult = when (registrationId) {
            "github" -> authenticationService.authenticateGitHubUser(
                oauthUser.attributes["id"].toString(),
                oauthUser.attributes["login"] as String
            )

            else -> throw InternalAuthenticationServiceException("Unsupported oauth integration")
        }


        authDataResult.onSuccess { authData ->
            val accessCookie = createAccessCookie(authData.accessToken, authData.accessTokenExpirationTime)
            val refreshCookie = createRefreshCookie(authData.refreshToken, authData.refreshTokenExpirationTime)
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            response.sendRedirect("${frontendBaseUrl}/login/oauth-success")
        }.onFailure { ex -> throw InternalAuthenticationServiceException("Failed to authenticate", ex) }
    }
}
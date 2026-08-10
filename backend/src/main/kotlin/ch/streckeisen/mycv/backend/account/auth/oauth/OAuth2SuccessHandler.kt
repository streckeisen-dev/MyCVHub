package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.auth.AuthTokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

private const val OAUTH_SUCCESS_REDIRECT = "login/oauth-success"

@Component
class OAuth2SuccessHandler(
    private val authTokenService: AuthTokenService,
    private val oAuthIntegrationService: OAuthIntegrationService,
    @param:Value(value = $$"${my-cv.frontend.base-url}")
    private val frontendBaseUrl: String,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauthAuthentication = authentication as OAuth2AuthenticationToken
        val oauthUser = oauthAuthentication.principal
        val registrationId = oauthAuthentication.authorizedClientRegistrationId

        if (oauthUser == null) {
            logger.error { "OAuth2 authentication unsuccessful" }
            throw InternalAuthenticationServiceException("Failed to authenticate")
        }

        val account = when (registrationId) {
            "github" -> {
                val oauthId = oauthUser.attributes["id"].toString()
                oAuthIntegrationService.getOrCreateOAuthAccount(
                    oauthAuthentication,
                    oauthId,
                    OAuthType.GITHUB
                )
            }

            else -> throw InternalAuthenticationServiceException("Unsupported oauth integration")
            }.getOrElse {
                logger.error(it) { "OAuth2 authentication unsuccessful" }
                throw InternalAuthenticationServiceException("Failed to authenticate", it)
            }

        authTokenService.generateAuthData(account.username)
            .onSuccess { authTokens ->
                val accessCookie =
                    authTokenService.createAccessCookie(authTokens.accessToken, authTokens.accessTokenExpirationTime / 1000)
                val refreshCookie =
                    authTokenService.createRefreshCookie(authTokens.refreshToken, authTokens.refreshTokenExpirationTime / 1000)
                response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
                response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

                val state = request.getParameter("state")
                val redirect = if (state != null) {
                    MyCvOAuth2AuthorizationRequestResolver.extractRedirectFromState(state)
                } else null

                val redirectParameter = redirect?.let {
                    "?redirect=${URLEncoder.encode(it, StandardCharsets.UTF_8)}"
                } ?: ""
                response.sendRedirect("${frontendBaseUrl}/$OAUTH_SUCCESS_REDIRECT$redirectParameter")
            }.onFailure { ex -> throw InternalAuthenticationServiceException("Failed to create auth tokens", ex) }
    }
}

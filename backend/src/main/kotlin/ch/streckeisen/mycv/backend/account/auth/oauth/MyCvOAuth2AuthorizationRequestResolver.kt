package ch.streckeisen.mycv.backend.account.auth.oauth

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.util.Base64

class MyCvOAuth2AuthorizationRequestResolver(
    private val defaultResolver: OAuth2AuthorizationRequestResolver
) : OAuth2AuthorizationRequestResolver {
    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request)
        return customizeAuthorizationRequest(authorizationRequest, request)
    }

    override fun resolve(
        request: HttpServletRequest,
        clientRegistrationId: String
    ): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request, clientRegistrationId)
        return customizeAuthorizationRequest(authorizationRequest, request)
    }

    private fun customizeAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest
    ): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null) {
            return null
        }
        val redirect = request.getParameter("redirect")
        val state = String(Base64.getUrlEncoder().encode("redirect=$redirect".toByteArray()))

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .state(state)
            .build()
    }
}
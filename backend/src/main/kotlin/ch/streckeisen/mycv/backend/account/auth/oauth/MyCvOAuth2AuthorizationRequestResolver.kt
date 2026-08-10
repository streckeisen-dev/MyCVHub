package ch.streckeisen.mycv.backend.account.auth.oauth

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

private const val OAUTH_STATE_KEY = "oauthState"
private const val REDIRECT_KEY = "redirect"
private val SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

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
        val state = createState(authorizationRequest.state, redirect)

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .state(state)
            .build()
    }

    companion object {
        fun createState(oauthState: String?, redirect: String?): String {
            val encodedParams = listOfNotNull(
                oauthState?.let { OAUTH_STATE_KEY to it },
                redirect?.takeIf { isSafeRedirect(it) }?.let { REDIRECT_KEY to it }
            ).joinToString("&") { (key, value) ->
                "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
            }

            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(encodedParams.toByteArray(StandardCharsets.UTF_8))
        }

        fun extractRedirectFromState(state: String): String? =
            decodeState(state)[REDIRECT_KEY]?.takeIf { isSafeRedirect(it) }

        private fun decodeState(state: String): Map<String, String> =
            runCatching {
                String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8)
                    .split("&")
                    .filter { it.isNotBlank() }
                    .associate {
                        val parts = it.split("=", limit = 2)
                        val value = parts.getOrElse(1) { "" }
                        parts[0] to URLDecoder.decode(value, StandardCharsets.UTF_8)
                    }
            }.getOrDefault(emptyMap())

        private fun isSafeRedirect(redirect: String): Boolean =
            redirect.startsWith("/") && !redirect.startsWith("//") && !SCHEME_REGEX.containsMatchIn(redirect)
    }
}

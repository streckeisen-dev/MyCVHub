package ch.streckeisen.mycv.backend.account.auth.oauth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2AuthorizationRequest"
private const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_MAX_AGE_SECONDS = 180L
private const val HMAC_ALGORITHM = "HmacSHA256"

@Component
class OAuth2AuthorizationRequestCookieRepository(
    @param:Value($$"${my-cv.security.jwt.access.secret}") private val cookieSigningSecret: String
) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        request.cookies
            ?.firstOrNull { it.name == OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME }
            ?.value
            ?.let { deserialize(it) }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie.from(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(authorizationRequest))
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/oauth2")
                .maxAge(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_MAX_AGE_SECONDS)
                .sameSite("Lax")
                .build()
                .toString()
        )
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        val authorizationRequest = loadAuthorizationRequest(request)
        expireCookie(response)
        return authorizationRequest
    }

    private fun serialize(authorizationRequest: OAuth2AuthorizationRequest): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(SerializationUtils.serialize(authorizationRequest))
            .let { payload -> "$payload.${sign(payload)}" }

    private fun deserialize(value: String): OAuth2AuthorizationRequest? =
        runCatching {
            val parts = value.split(".", limit = 2)
            if (parts.size != 2 || !isSignatureValid(parts[0], parts[1])) {
                return null
            }
            SerializationUtils.deserialize(Base64.getUrlDecoder().decode(parts[0])) as? OAuth2AuthorizationRequest
        }.getOrNull()

    private fun sign(payload: String): String =
        Mac.getInstance(HMAC_ALGORITHM)
            .apply {
                init(SecretKeySpec(cookieSigningSecret.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM))
            }
            .doFinal(payload.toByteArray(Charsets.UTF_8))
            .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }

    private fun isSignatureValid(payload: String, signature: String): Boolean =
        MessageDigest.isEqual(sign(payload).toByteArray(Charsets.UTF_8), signature.toByteArray(Charsets.UTF_8))

    private fun expireCookie(response: HttpServletResponse) {
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie.from(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/oauth2")
                .maxAge(0)
                .sameSite("Lax")
                .build()
                .toString()
        )
    }
}

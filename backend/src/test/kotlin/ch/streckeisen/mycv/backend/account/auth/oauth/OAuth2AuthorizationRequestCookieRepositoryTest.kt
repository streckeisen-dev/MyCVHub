package ch.streckeisen.mycv.backend.account.auth.oauth

import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class OAuth2AuthorizationRequestCookieRepositoryTest {
    private val repository = OAuth2AuthorizationRequestCookieRepository("test-secret")

    @Test
    fun testSaveAndLoadAuthorizationRequest() {
        val authorizationRequest = authorizationRequest()
        val response = MockHttpServletResponse()

        repository.saveAuthorizationRequest(authorizationRequest, MockHttpServletRequest(), response)
        val cookie = requireNotNull(response.getCookie("oauth2AuthorizationRequest"))

        assertNotNull(cookie)
        val request = MockHttpServletRequest()
        request.setCookies(Cookie(cookie.name, cookie.value))

        val loadedAuthorizationRequest = repository.loadAuthorizationRequest(request)

        assertEquals(authorizationRequest.state, loadedAuthorizationRequest?.state)
        assertEquals(authorizationRequest.clientId, loadedAuthorizationRequest?.clientId)
        assertEquals(authorizationRequest.redirectUri, loadedAuthorizationRequest?.redirectUri)
    }

    @Test
    fun testLoadAuthorizationRequestWithTamperedCookieReturnsNull() {
        val response = MockHttpServletResponse()
        repository.saveAuthorizationRequest(authorizationRequest(), MockHttpServletRequest(), response)
        val cookie = requireNotNull(response.getCookie("oauth2AuthorizationRequest"))
        val request = MockHttpServletRequest()
        request.setCookies(Cookie(cookie.name, "${cookie.value}tampered"))

        val loadedAuthorizationRequest = repository.loadAuthorizationRequest(request)

        assertEquals(null, loadedAuthorizationRequest)
    }

    @Test
    fun testRemoveAuthorizationRequestExpiresCookie() {
        val response = MockHttpServletResponse()
        repository.saveAuthorizationRequest(authorizationRequest(), MockHttpServletRequest(), response)
        val cookie = requireNotNull(response.getCookie("oauth2AuthorizationRequest"))
        val request = MockHttpServletRequest()
        request.setCookies(Cookie(cookie.name, cookie.value))
        val removeResponse = MockHttpServletResponse()

        val removedAuthorizationRequest = repository.removeAuthorizationRequest(request, removeResponse)

        assertNotNull(removedAuthorizationRequest)
        assertEquals(0, requireNotNull(removeResponse.getCookie("oauth2AuthorizationRequest")).maxAge)
    }

    private fun authorizationRequest(): OAuth2AuthorizationRequest =
        OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://github.com/login/oauth/authorize")
            .clientId("client-id")
            .redirectUri("https://mycvhub.ch/api/auth/oauth2/callback/github")
            .state("state")
            .scope("read:user", "user:email")
            .build().let {
                OAuth2AuthorizationRequest.from(it)
                    .authorizationRequestUri("https://github.com/login/oauth/authorize?client_id=client-id")
                    .build()
            }
}

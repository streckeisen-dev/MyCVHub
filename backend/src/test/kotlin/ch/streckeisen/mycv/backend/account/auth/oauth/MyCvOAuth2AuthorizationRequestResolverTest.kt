package ch.streckeisen.mycv.backend.account.auth.oauth

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class MyCvOAuth2AuthorizationRequestResolverTest {
    private lateinit var baseResolver: OAuth2AuthorizationRequestResolver
    private lateinit var myCvOAuth2AuthorizationRequestResolver: MyCvOAuth2AuthorizationRequestResolver

    @BeforeEach
    fun setup() {
        baseResolver = mockk()
        myCvOAuth2AuthorizationRequestResolver = MyCvOAuth2AuthorizationRequestResolver(baseResolver)
    }

    @Test
    fun testResolveReturningNull() {
        every { baseResolver.resolve(any()) } returns null

        val result = myCvOAuth2AuthorizationRequestResolver.resolve(mockk())

        assertNull(result)
    }

    @Test
    fun testResolveWithTwoArgsReturningNull() {
        every { baseResolver.resolve(any(), any()) } returns null

        val result = myCvOAuth2AuthorizationRequestResolver.resolve(mockk(), "")

        assertNull(result)
    }

    @Test
    fun testRedirectIsPassedOn() {
        every { baseResolver.resolve(any()) } returns mockk<OAuth2AuthorizationRequest> {
            every { state } returns "spring-state"
            every { grantType } returns AuthorizationGrantType.AUTHORIZATION_CODE
            every { authorizationUri } returns "https://test.example.com"
            every { clientId } returns "test"
            every { redirectUri } returns "https://redirect.example.com"
            every { scopes } returns setOf("test")
            every { additionalParameters } returns mapOf()
            every { responseType } returns OAuth2AuthorizationResponseType.CODE
            every { attributes } returns mapOf()
            every { authorizationRequestUri } returns "https://test.example.com"
        }
        val request = mockk<HttpServletRequest> {
            every { getParameter("redirect") } returns "/ui/test"
        }

        val result = myCvOAuth2AuthorizationRequestResolver.resolve(request)

        assertNotNull(result)
        assertNotNull(result!!.state)
        val decodedState = String(Base64.getUrlDecoder().decode(result.state), StandardCharsets.UTF_8)
        val params = decodedState.split("&").associate {
            val (key, value) = it.split("=", limit = 2)
            key to URLDecoder.decode(value, StandardCharsets.UTF_8)
        }
        assertEquals("spring-state", params["oauthState"])
        assertEquals("/ui/test", params["redirect"])
    }

    @Test
    fun testExtractRedirectFromState() {
        val state = MyCvOAuth2AuthorizationRequestResolver.createState(
            "spring-state",
            "/ui/test?foo=bar&baz=qux"
        )

        assertEquals(
            "/ui/test?foo=bar&baz=qux",
            MyCvOAuth2AuthorizationRequestResolver.extractRedirectFromState(state)
        )
    }

    @Test
    fun testUnsafeRedirectIsIgnored() {
        val state = MyCvOAuth2AuthorizationRequestResolver.createState(
            "spring-state",
            "https://evil.example.com"
        )

        assertNull(MyCvOAuth2AuthorizationRequestResolver.extractRedirectFromState(state))
    }
}

package ch.streckeisen.mycv.backend.account.auth.oauth

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
            every { state } returns null
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
        assertNotNull(result.state)
        val decodedState = Base64.getUrlDecoder().decode(result.state).toString(Charsets.UTF_8)
        assertEquals("redirect=/ui/test", decodedState)
    }
}
package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.auth.AuthenticationValidationService
import ch.streckeisen.mycv.backend.github.GithubException
import ch.streckeisen.mycv.backend.github.GithubService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OAuthIntegrationServiceTest {
    private lateinit var oauthIntegrationRepository: OauthIntegrationRepository
    private lateinit var authenticationValidationService: AuthenticationValidationService
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var authorizedClientService: OAuth2AuthorizedClientService
    private lateinit var githubService: GithubService
    private lateinit var accountService: ApplicantAccountService
    private lateinit var oAuthIntegrationService: OAuthIntegrationService

    @BeforeEach
    fun setup() {
        oauthIntegrationRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findById(eq(OAuthEntityId("existingOAuthUser", OAuthType.GITHUB))) } returns Optional.of(
                OAuthIntegrationEntity(OAuthEntityId("existingOAuthUser", OAuthType.GITHUB), mockk {
                    every { id } returns 1
                })
            )
        }
        authenticationValidationService = mockk {}
        applicantAccountRepository = mockk {}
        authorizedClientService = mockk {}
        githubService = mockk {}
        accountService = mockk {}

        oAuthIntegrationService = OAuthIntegrationService(
            oauthIntegrationRepository,
            authenticationValidationService,
            applicantAccountRepository,
            authorizedClientService,
            githubService,
            accountService
        )
    }

    @Test
    fun testGetOAuthAccountWithExistingOrCreateOAuthAccount() {
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
        }

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, "existingOAuthUser", OAuthType.GITHUB)

        assertTrue(result.isSuccess)
        val account = result.getOrNull()
        assertNotNull(account)
        assertEquals(1, account.id)
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testGetOAuthAccountWithNewOAuthAccount() {
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns "newOAuthUser"
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns "accessToken"
        }
        every { githubService.getUserEmail("newOAuthUser", "accessToken") } returns Result.success("test@email.com")
        every { applicantAccountRepository.findByEmail(any()) } returns Optional.empty()
        every { authenticationValidationService.validateOAuthSignupRequest(eq("test@email.com")) } returns Result.success(
            Unit
        )
        val accountSlot = slot<ApplicantAccountEntity>()
        every { applicantAccountRepository.save(capture(accountSlot)) } returns mockk {
            every { id } returns 10
        }
        val oauthIntegrationSlot = slot<OAuthIntegrationEntity>()
        every { oauthIntegrationRepository.save(capture(oauthIntegrationSlot)) } returns mockk()

        val result = oAuthIntegrationService.getOrCreateOAuthAccount(authentication, "newOAuthUser", OAuthType.GITHUB)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { applicantAccountRepository.save(any()) }
        verify(exactly = 1) { oauthIntegrationRepository.save(any()) }

        assertNotNull(accountSlot.captured)
        assertEquals("test@email.com", accountSlot.captured.username)
        assertTrue(accountSlot.captured.isOAuthUser)
        assertFalse(accountSlot.captured.isVerified)
        assertNull(accountSlot.captured.password)
        assertNull(accountSlot.captured.accountDetails)
        assertTrue(accountSlot.captured.oauthIntegrations.isEmpty())

        assertNotNull(oauthIntegrationSlot.captured)
        assertEquals("newOAuthUser", oauthIntegrationSlot.captured.id.id)
        assertEquals(OAuthType.GITHUB, oauthIntegrationSlot.captured.id.type)
        assertEquals(10, oauthIntegrationSlot.captured.account.id)
    }

    @Test
    fun testGetOAuthAccountWithNewUsernameTaken() {
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns "newOAuthUser"
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns "accessToken"
        }
        every { githubService.getUserEmail("newOAuthUser", "accessToken") } returns Result.success("test@email.com")
        every { applicantAccountRepository.findByEmail(any()) } returns Optional.empty()
        every { authenticationValidationService.validateOAuthSignupRequest(eq("test@email.com")) } returns Result.failure(
            IllegalArgumentException("Username already taken")
        )

        val result = oAuthIntegrationService.getOrCreateOAuthAccount(authentication, "newOAuthUser", OAuthType.GITHUB)

        assertTrue(result.isFailure)
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
    }

    @Test
    fun testGetOAuthAccountWithExistingNonAssociatedAccount() {
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns "unassociatedOAuthUser"
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns "accessToken"
        }
        every {
            githubService.getUserEmail(
                "unassociatedOAuthUser",
                "accessToken"
            )
        } returns Result.success("unassociated@email.com")
        val oauthAccount = mockk<ApplicantAccountEntity> {
            every { id } returns 5
            every { accountDetails } returns mockk()
            every { isVerified } returns true
        }
        every { applicantAccountRepository.findByEmail(eq("unassociated@email.com")) } returns Optional.of(oauthAccount)
        val oauthIntegrationSlot = slot<OAuthIntegrationEntity>()
        every { oauthIntegrationRepository.save(capture(oauthIntegrationSlot)) } returns mockk {
            every { account } returns oauthAccount
        }

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, "unassociatedOAuthUser", OAuthType.GITHUB)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
        assertNotNull(oauthIntegrationSlot.captured)
        assertEquals("unassociatedOAuthUser", oauthIntegrationSlot.captured.id.id)
        assertEquals(OAuthType.GITHUB, oauthIntegrationSlot.captured.id.type)
        assertEquals(5, oauthIntegrationSlot.captured.account.id)
    }

    @Test
    fun testGetOAuthAccountWithExistingNonAssociatedAndNonVerifiedAccount() {
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns "unassociatedOAuthUser"
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns "accessToken"
        }
        every {
            githubService.getUserEmail(
                "unassociatedOAuthUser",
                "accessToken"
            )
        } returns Result.success("unassociated@email.com")
        every { applicantAccountRepository.findByEmail(eq("unassociated@email.com")) } returns Optional.of(mockk {
            every { id } returns 5
            every { accountDetails } returns null
            every { isVerified } returns true
        })

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, "unassociatedOAuthUser", OAuthType.GITHUB)

        assertTrue(result.isFailure)
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testGetOAuthAccountWithFailedEmailRequest() {
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns "unassociatedOAuthUser"
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns "accessToken"
        }
        every {
            githubService.getUserEmail(
                "unassociatedOAuthUser",
                "accessToken"
            )
        } returns Result.failure(GithubException("Failed to get email"))

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, "unassociatedOAuthUser", OAuthType.GITHUB)

        assertTrue(result.isFailure)
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }
}
package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.auth.AuthTokenService
import ch.streckeisen.mycv.backend.account.auth.AuthenticationValidationService
import ch.streckeisen.mycv.backend.github.GithubException
import ch.streckeisen.mycv.backend.github.GithubService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import java.util.Optional

class OAuthIntegrationServiceTest {
    private lateinit var oauthIntegrationRepository: OauthIntegrationRepository
    private lateinit var authenticationValidationService: AuthenticationValidationService
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var authorizedClientService: OAuth2AuthorizedClientService
    private lateinit var githubService: GithubService
    private lateinit var accountService: ApplicantAccountService
    private lateinit var authTokenService: AuthTokenService
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
        authTokenService = mockk {}

        oAuthIntegrationService = OAuthIntegrationService(
            oauthIntegrationRepository,
            authenticationValidationService,
            applicantAccountRepository,
            authorizedClientService,
            githubService,
            accountService,
            authTokenService
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
        assertEquals(1, account!!.id)
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testGetOAuthAccountWithNewOAuthAccount() {
        val oauthId = "newOAuthUser"
        val accountEmail = "test@email.com"
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns oauthId
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns "accessToken"
        }
        every { githubService.getUserEmail(oauthId, "accessToken") } returns Result.success(accountEmail)
        every { applicantAccountRepository.findByEmail(any()) } returns Optional.empty()
        every { authenticationValidationService.validateOAuthSignupRequest(eq(accountEmail)) } returns Result.success(
            Unit
        )
        val accountSlot = slot<ApplicantAccountEntity>()
        every { applicantAccountRepository.save(capture(accountSlot)) } returns mockk {
            every { id } returns 10
        }
        val oauthIntegrationSlot = slot<OAuthIntegrationEntity>()
        every { oauthIntegrationRepository.save(capture(oauthIntegrationSlot)) } returns mockk()

        val result = oAuthIntegrationService.getOrCreateOAuthAccount(authentication, oauthId, OAuthType.GITHUB)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { applicantAccountRepository.save(any()) }
        verify(exactly = 1) { oauthIntegrationRepository.save(any()) }

        assertNotNull(accountSlot.captured)
        assertEquals(accountEmail, accountSlot.captured.username)
        assertTrue(accountSlot.captured.isOAuthUser)
        assertFalse(accountSlot.captured.isVerified)
        assertNull(accountSlot.captured.password)
        assertNull(accountSlot.captured.accountDetails)
        assertTrue(accountSlot.captured.oauthIntegrations.isEmpty())

        assertNotNull(oauthIntegrationSlot.captured)
        assertEquals(oauthId, oauthIntegrationSlot.captured.id.id)
        assertEquals(OAuthType.GITHUB, oauthIntegrationSlot.captured.id.type)
        assertEquals(10, oauthIntegrationSlot.captured.account.id)
    }

    @Test
    fun testGetOAuthAccountWithNewUsernameTaken() {
        val oauthId = "newOAuthUser"
        val accountEmail = "test@email.com"
        val token = "accessToken"
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns oauthId
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns token
        }
        every { githubService.getUserEmail(oauthId, token) } returns Result.success(accountEmail)
        every { applicantAccountRepository.findByEmail(any()) } returns Optional.empty()
        every { authenticationValidationService.validateOAuthSignupRequest(eq(accountEmail)) } returns Result.failure(
            IllegalArgumentException("Username already taken")
        )

        val result = oAuthIntegrationService.getOrCreateOAuthAccount(authentication, oauthId, OAuthType.GITHUB)

        assertTrue(result.isFailure)
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
    }

    @Test
    fun testGetOAuthAccountWithExistingNonAssociatedAccount() {
        val oauthId = "unassociatedOAuthUser"
        val accountEmail = "unassociated@email.com"
        val token = "accessToken"
        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns oauthId
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns token
        }
        every {
            githubService.getUserEmail(
                oauthId,
                token
            )
        } returns Result.success(accountEmail)
        val oauthAccount = mockk<ApplicantAccountEntity> {
            every { id } returns 5
            every { accountDetails } returns mockk()
            every { isVerified } returns true
        }
        every { applicantAccountRepository.findByEmail(eq(accountEmail)) } returns Optional.of(oauthAccount)
        val oauthIntegrationSlot = slot<OAuthIntegrationEntity>()
        every { oauthIntegrationRepository.save(capture(oauthIntegrationSlot)) } returns mockk {
            every { account } returns oauthAccount
        }

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, oauthId, OAuthType.GITHUB)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
        assertNotNull(oauthIntegrationSlot.captured)
        assertEquals(oauthId, oauthIntegrationSlot.captured.id.id)
        assertEquals(OAuthType.GITHUB, oauthIntegrationSlot.captured.id.type)
        assertEquals(5, oauthIntegrationSlot.captured.account.id)
    }

    @Test
    fun testGetOAuthAccountWithExistingNonAssociatedAndNonVerifiedAccount() {
        val oauthId = "unassociatedOAuthUser"
        val accountEmail = "unassociated@email.com"
        val token = "accessToken"

        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns oauthId
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns token
        }
        every {
            githubService.getUserEmail(
                oauthId,
                token
            )
        } returns Result.success(accountEmail)
        every { applicantAccountRepository.findByEmail(eq(accountEmail)) } returns Optional.of(mockk {
            every { id } returns 5
            every { accountDetails } returns null
            every { isVerified } returns true
        })

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, oauthId, OAuthType.GITHUB)

        assertTrue(result.isFailure)
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }

    @Test
    fun testGetOAuthAccountWithFailedEmailRequest() {
        val oauthId = "unassociatedOAuthUser"
        val token = "accessToken"

        val authentication = mockk<OAuth2AuthenticationToken> {
            every { authorizedClientRegistrationId } returns "githubRegistrationId"
            every { name } returns oauthId
        }
        every { authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any()) } returns mockk {
            every { accessToken.tokenValue } returns token
        }
        every {
            githubService.getUserEmail(
                oauthId,
                token
            )
        } returns Result.failure(GithubException("Failed to get email"))

        val result =
            oAuthIntegrationService.getOrCreateOAuthAccount(authentication, oauthId, OAuthType.GITHUB)

        assertTrue(result.isFailure)
        verify(exactly = 0) { oauthIntegrationRepository.save(any()) }
        verify(exactly = 0) { applicantAccountRepository.save(any()) }
    }
}
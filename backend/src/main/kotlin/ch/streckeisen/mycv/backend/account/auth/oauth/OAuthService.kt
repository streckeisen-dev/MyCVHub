package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.auth.AuthenticationValidationService
import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.OAuthSignupRequestDto
import ch.streckeisen.mycv.backend.github.GithubService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private val logger = KotlinLogging.logger {}

@Service
class OAuthService(
    private val oauthIntegrationService: OAuthIntegrationService,
    private val authenticationValidationService: AuthenticationValidationService,
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val githubService: GithubService,
    private val accountService: ApplicantAccountService
) {
    @Transactional
    fun getOAuthAccount(
        authentication: OAuth2AuthenticationToken,
        oauthId: String,
        oAuthType: OAuthType
    ): Result<ApplicantAccountEntity> {
        val registrationId = authentication.authorizedClientRegistrationId
        return oauthIntegrationService.findById(oauthId, oAuthType).fold(
            onSuccess = { oauthIntegration ->
                Result.success(oauthIntegration.account!!)
            },
            onFailure = {
                val accessToken = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
                    registrationId,
                    authentication.name
                ).accessToken.tokenValue
                val userEmail = getOAuthUserEmail(oauthId, oAuthType, accessToken)
                    .getOrElse { return Result.failure(it) }
                val account = applicantAccountRepository.findByEmail(userEmail)
                    .getOrElse {
                        logger.info { "Successful OAuth authentication, but couldn't find matching account. Create incomplete account..." }
                        return createOAuthAccount(userEmail, oAuthType, oauthId)
                    }
                if (account.isVerified) {
                    logger.info { "[Account ${account.id}] Adding GitHub integration for account" }
                    oauthIntegrationService.addOAuthIntegration(account, oauthId, oAuthType)
                    Result.success(account)
                } else {
                    Result.failure(OAuth2AuthenticationException("Cannot add OAuth integration to unverified account"))
                }
            }
        )
    }

    fun completeSignup(accountId: Long, oauthSignupRequest: OAuthSignupRequestDto): Result<ApplicantAccountEntity> {
        val accountUpdate = AccountUpdateDto(
            username = oauthSignupRequest.username,
            firstName = oauthSignupRequest.firstName,
            lastName = oauthSignupRequest.lastName,
            email = oauthSignupRequest.email,
            phone = oauthSignupRequest.phone,
            birthday = oauthSignupRequest.birthday,
            street = oauthSignupRequest.street,
            houseNumber = oauthSignupRequest.houseNumber,
            postcode = oauthSignupRequest.postcode,
            city = oauthSignupRequest.city,
            country = oauthSignupRequest.country
        )
        return accountService.update(accountId, accountUpdate)
            .onFailure { return Result.failure(it) }
    }

    @Transactional
    private fun createOAuthAccount(
        username: String,
        oauthType: OAuthType,
        oauthId: String
    ): Result<ApplicantAccountEntity> {
        authenticationValidationService.validateOAuthSignupRequest(username)
            .onFailure { return Result.failure(it) }

        val oauthAccount = ApplicantAccountEntity(
            username,
            null,
            true,
            false
        )
        val account = applicantAccountRepository.save(oauthAccount)
        oauthIntegrationService.addOAuthIntegration(account, oauthId, oauthType)
        logger.info { "[Account ${account.id}] Created new oauth account" }
        return Result.success(account)
    }

    private fun getOAuthUserEmail(oauthId: String, oAuthType: OAuthType, accessToken: String): Result<String> {
        return when (oAuthType) {
            OAuthType.GITHUB -> Result.success(githubService.getUserEmail(oauthId, accessToken))
                .getOrElse { Result.failure(it) }

            OAuthType.LINKED_IN -> TODO()
        }
    }
}
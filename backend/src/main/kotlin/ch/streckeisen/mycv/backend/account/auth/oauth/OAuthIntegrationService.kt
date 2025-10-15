package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.auth.AuthTokenService
import ch.streckeisen.mycv.backend.account.auth.AuthTokens
import ch.streckeisen.mycv.backend.account.auth.AuthenticationValidationService
import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.OAuthSignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.github.GithubService
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
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
class OAuthIntegrationService(
    private val oauthIntegrationRepository: OauthIntegrationRepository,
    private val authenticationValidationService: AuthenticationValidationService,
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val githubService: GithubService,
    private val accountService: ApplicantAccountService,
    private val authTokenService: AuthTokenService
) {
    @Transactional(readOnly = true)
    fun findById(oauthId: String, oauthType: OAuthType): Result<OAuthIntegrationEntity> {
        return oauthIntegrationRepository.findById(OAuthEntityId(oauthId, oauthType))
            .map { Result.success(it) }
            .orElse(Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.oauth.notFound")))
    }

    @Transactional
    fun getOrCreateOAuthAccount(
        authentication: OAuth2AuthenticationToken,
        oauthId: String,
        oAuthType: OAuthType
    ): Result<ApplicantAccountEntity> {
        val registrationId = authentication.authorizedClientRegistrationId
        return findById(oauthId, oAuthType).fold(
            onSuccess = { oauthIntegration ->
                Result.success(oauthIntegration.account)
            },
            onFailure = {
                associateOrCreateOAuthAccount(registrationId, authentication, oauthId, oAuthType)
            }
        )
    }

    fun completeSignup(accountId: Long, oauthSignupRequest: OAuthSignupRequestDto): Result<AuthTokens> {
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
            country = oauthSignupRequest.country,
            language = oauthSignupRequest.language
        )
        val account = accountService.update(accountId, accountUpdate)
            .getOrElse { return Result.failure(it) }
        return authTokenService.generateAuthData(account.username)
    }

    @Transactional
    private fun associateOrCreateOAuthAccount(
        registrationId: String,
        authentication: OAuth2AuthenticationToken,
        oauthId: String,
        oAuthType: OAuthType
    ): Result<ApplicantAccountEntity> {
        val accessToken = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
            registrationId,
            authentication.name
        ).accessToken.tokenValue
        val userEmail = getOAuthUserEmail(oauthId, oAuthType, accessToken)
            .getOrElse { return Result.failure(it) }
        val account = applicantAccountRepository.findByEmail(userEmail)
            .getOrElse {
                logger.debug { "Successful OAuth authentication, but couldn't find matching account. Creating incomplete account..." }
                return createIncompleteOAuthAccount(userEmail, oAuthType, oauthId)
                    .fold(
                        onSuccess = { oauthAccount ->
                            logger.info { "[Account ${oauthAccount.id}] Created new oauth account" }
                            Result.success(oauthAccount)
                        },
                        onFailure = {
                            logger.error(it) { "Failed to create oauth account" }
                            return Result.failure(it)
                        }
                    )
            }
        return if (AccountStatus.ofAccount(account) == AccountStatus.VERIFIED) {
            logger.debug { "[Account ${account.id}] Adding GitHub integration for account" }
            addOAuthIntegration(account, oauthId, oAuthType)
                .fold(
                    onSuccess = { oauthIntegration ->
                        logger.debug { "[Account ${account.id}] Added GitHub integration for account" }
                        Result.success(oauthIntegration.account)
                    },
                    onFailure = {
                        logger.error(it) { "Account [${account.id}] Failed to associate oauth account" }
                        Result.failure(it)
                    }
                )
        } else {
            logger.debug { "[Account ${account.id}] Cannot add OAuth integration to unverified account" }
            Result.failure(OAuth2AuthenticationException("Cannot add OAuth integration to unverified account"))
        }
    }

    private fun createIncompleteOAuthAccount(
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
        addOAuthIntegration(account, oauthId, oauthType)
            .onFailure { return Result.failure(it) }
        return Result.success(account)
    }

    @Transactional
    private fun addOAuthIntegration(
        account: ApplicantAccountEntity,
        oauthId: String,
        oAuthType: OAuthType
    ): Result<OAuthIntegrationEntity> {
        // without the Result as return type, the function would create a compilation error
        return Result.success(
            oauthIntegrationRepository.save(
                OAuthIntegrationEntity(
                    OAuthEntityId(oauthId, oAuthType),
                    account
                )
            )
        )
    }

    private fun getOAuthUserEmail(oauthId: String, oAuthType: OAuthType, accessToken: String): Result<String> {
        return when (oAuthType) {
            OAuthType.GITHUB -> Result.success(githubService.getUserEmail(oauthId, accessToken))
                .getOrElse { Result.failure(it) }

            OAuthType.LINKED_IN -> TODO()
        }
    }
}
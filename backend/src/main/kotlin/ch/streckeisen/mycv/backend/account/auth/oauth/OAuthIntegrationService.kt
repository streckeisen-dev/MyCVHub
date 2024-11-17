package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuthIntegrationService(
    private val oauthIntegrationRepository: OauthIntegrationRepository
) {
    @Transactional(readOnly = true)
    fun findById(oauthId: String, oauthType: OAuthType): Result<OAuthIntegrationEntity> {
        return oauthIntegrationRepository.findById(OAuthEntityId(oauthId, oauthType))
            .map { Result.success(it) }
            .orElse(Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.oauth.notFound")))
    }

    @Transactional
    fun addOAuthIntegration(
        account: ApplicantAccountEntity,
        oauthId: String,
        oAuthType: OAuthType
    ): OAuthIntegrationEntity {
        return oauthIntegrationRepository.save(
            OAuthIntegrationEntity(
                OAuthEntityId(oauthId, oAuthType),
                account
            )
        )
    }
}
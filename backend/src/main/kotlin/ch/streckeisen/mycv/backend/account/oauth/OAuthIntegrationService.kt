package ch.streckeisen.mycv.backend.account.oauth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuthIntegrationService(
    private val oauthIntegrationRepository: OauthIntegrationRepository
) {
    @Transactional(readOnly = true)
    fun findByGithubId(githubId: String): Result<OAuthIntegrationEntity> {
        return oauthIntegrationRepository.findById(OAuthEntityId(githubId, OAuthType.GITHUB))
            .map { Result.success(it) }
            .orElse(Result.failure(EntityNotFoundException("OAuth account not found")))
    }

    @Transactional
    fun addGithubIntegration(account: ApplicantAccountEntity, githubId: String): OAuthIntegrationEntity {
        return oauthIntegrationRepository.save(
            OAuthIntegrationEntity(
                OAuthEntityId(githubId, OAuthType.GITHUB),
                "",
                account
            )
        )
    }
}
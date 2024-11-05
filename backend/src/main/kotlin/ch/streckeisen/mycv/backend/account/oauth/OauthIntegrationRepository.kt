package ch.streckeisen.mycv.backend.account.oauth

import org.springframework.data.repository.CrudRepository

interface OauthIntegrationRepository : CrudRepository<OAuthIntegrationEntity, OAuthEntityId> {
}
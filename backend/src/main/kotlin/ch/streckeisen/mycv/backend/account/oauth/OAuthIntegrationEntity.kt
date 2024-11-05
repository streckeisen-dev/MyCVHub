package ch.streckeisen.mycv.backend.account.oauth

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "oauth_integration_entity")
class OAuthIntegrationEntity(
    @EmbeddedId
    val id: OAuthEntityId,
    val accessToken: String,
    @ManyToOne(fetch = FetchType.EAGER)
    val account: ApplicantAccountEntity
) {
}
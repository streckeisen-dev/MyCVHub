package ch.streckeisen.mycv.backend.account.auth.oauth

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class OAuthEntityId(
    val id: String,
    @Enumerated(EnumType.STRING)
    val type: OAuthType
)
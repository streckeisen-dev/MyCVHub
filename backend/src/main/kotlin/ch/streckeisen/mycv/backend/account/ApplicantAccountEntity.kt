package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.auth.oauth.OAuthIntegrationEntity
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationEntity
import ch.streckeisen.mycv.backend.application.ApplicationEntity
import ch.streckeisen.mycv.backend.applicationTemplate.ApplicationTemplateEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class ApplicantAccountEntity(
    var username: String,
    var password: String?,
    @Column(name = "is_oauth_user")
    var isOAuthUser: Boolean,
    var isVerified: Boolean,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var accountDetails: AccountDetailsEntity? = null,
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "account")
    var oauthIntegrations: List<OAuthIntegrationEntity> = emptyList(),

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "account")
    var profile: ProfileEntity? = null,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "account")
    var accountVerification: AccountVerificationEntity? = null,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "account")
    var applications: List<ApplicationEntity> = emptyList(),
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "account")
    var applicationTemplates: List<ApplicationTemplateEntity> = emptyList()
)
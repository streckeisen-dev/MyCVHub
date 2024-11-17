package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.auth.oauth.OAuthIntegrationEntity
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
import java.time.LocalDate

@Entity
class ApplicantAccountEntity(
    val username: String,
    val password: String?,
    @Column(name = "is_oauth_user")
    val isOAuthUser: Boolean,
    val isVerified: Boolean,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val accountDetails: AccountDetailsEntity? = null,
    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "account")
    val oauthIntegrations: List<OAuthIntegrationEntity> = emptyList(),

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "account")
    val profile: ProfileEntity? = null
)
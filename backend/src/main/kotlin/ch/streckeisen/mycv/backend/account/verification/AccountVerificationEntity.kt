package ch.streckeisen.mycv.backend.account.verification

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.time.LocalDateTime

@Entity
class AccountVerificationEntity(
    var token: String,
    var expirationDate: LocalDateTime,
    @OneToOne
    var account: ApplicantAccountEntity,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)
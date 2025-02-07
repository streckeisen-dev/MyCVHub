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
    val token: String,
    val expirationDate: LocalDateTime,
    @OneToOne
    val account: ApplicantAccountEntity,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
}
package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class ApplicationTemplateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val account: ApplicantAccountEntity,

    val name: String,
    val cvConfiguration: String,
    val documents: String?
) {
}
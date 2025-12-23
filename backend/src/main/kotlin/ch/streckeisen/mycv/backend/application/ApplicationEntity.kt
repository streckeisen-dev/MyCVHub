package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class ApplicationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val jobTitle: String,
    val company: String,
    @Enumerated(EnumType.STRING)
    val status: ApplicationStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
    val source: String? = null,
    val description: String? = null,

    @OneToMany(mappedBy = "application", fetch = FetchType.EAGER)
    val history: List<ApplicationHistoryEntity> = listOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    val account: ApplicantAccountEntity
)
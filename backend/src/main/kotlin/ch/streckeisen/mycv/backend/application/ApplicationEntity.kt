package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import jakarta.persistence.CascadeType
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
    var id: Long? = null,
    var jobTitle: String,
    var company: String,
    @Enumerated(EnumType.STRING)
    var status: ApplicationStatus,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime? = null,
    var source: String? = null,
    var description: String? = null,
    var isArchived: Boolean = false,

    @OneToMany(mappedBy = "application", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var history: List<ApplicationHistoryEntity> = listOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    var account: ApplicantAccountEntity
)
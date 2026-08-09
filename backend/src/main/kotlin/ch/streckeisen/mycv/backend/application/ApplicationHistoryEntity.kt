package ch.streckeisen.mycv.backend.application

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class ApplicationHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var source: ApplicationStatus,
    var target: ApplicationStatus,
    var comment: String? = null,
    var timestamp: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    var application: ApplicationEntity
)
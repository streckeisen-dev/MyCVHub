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
    val id: Long? = null,

    val source: ApplicationStatus,
    val target: ApplicationStatus,
    val comment: String? = null,
    val timestamp: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    val application: ApplicationEntity
)
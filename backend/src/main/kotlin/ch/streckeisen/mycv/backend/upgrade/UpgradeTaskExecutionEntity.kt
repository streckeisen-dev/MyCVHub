package ch.streckeisen.mycv.backend.upgrade

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class UpgradeTaskExecutionEntity(
    @Id
    val id: Int,
    val taskName: String,
    val executionDate: LocalDateTime
)
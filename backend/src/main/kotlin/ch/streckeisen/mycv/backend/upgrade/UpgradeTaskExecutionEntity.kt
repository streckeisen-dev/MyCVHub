package ch.streckeisen.mycv.backend.upgrade

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class UpgradeTaskExecutionEntity(
    @Id
    var id: Int,
    var taskName: String,
    var executionDate: LocalDateTime
)
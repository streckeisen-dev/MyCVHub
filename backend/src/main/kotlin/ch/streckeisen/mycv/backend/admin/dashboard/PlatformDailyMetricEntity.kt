package ch.streckeisen.mycv.backend.admin.dashboard

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class PlatformDailyMetricId(
    var metricDate: LocalDate? = null,
    var metricName: String? = null
) : Serializable

@Entity
@IdClass(PlatformDailyMetricId::class)
class PlatformDailyMetricEntity(
    @Id
    var metricDate: LocalDate,
    @Id
    var metricName: String,
    var metricValue: Long,
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

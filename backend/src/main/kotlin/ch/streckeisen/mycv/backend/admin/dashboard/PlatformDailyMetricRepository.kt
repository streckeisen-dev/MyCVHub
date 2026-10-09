package ch.streckeisen.mycv.backend.admin.dashboard

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface PlatformDailyMetricRepository : CrudRepository<PlatformDailyMetricEntity, PlatformDailyMetricId> {
    fun findFirstByMetricNameOrderByMetricDateDesc(metricName: String): PlatformDailyMetricEntity?

    fun findByMetricNameInAndMetricDateBetweenOrderByMetricDateAsc(
        metricNames: Collection<String>,
        from: LocalDate,
        to: LocalDate
    ): List<PlatformDailyMetricEntity>
}

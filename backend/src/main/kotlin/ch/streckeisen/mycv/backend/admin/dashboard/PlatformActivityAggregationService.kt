package ch.streckeisen.mycv.backend.admin.dashboard

import ch.streckeisen.mycv.backend.admin.activity.ActivityEventType
import ch.streckeisen.mycv.backend.admin.activity.UserActivityEventRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class PlatformActivityAggregationService(
    private val userActivityEventRepository: UserActivityEventRepository,
    private val platformDailyMetricRepository: PlatformDailyMetricRepository
) {
    @Transactional
    fun aggregateMissingCompletedDays(today: LocalDate = LocalDate.now()) {
        val yesterday = today.minusDays(1)
        val startDate = getNextDateToAggregate() ?: yesterday

        if (startDate.isAfter(yesterday)) {
            logger.info { "No completed platform activity days to aggregate" }
            return
        }

        generateSequence(startDate) { date -> date.plusDays(1) }
            .takeWhile { date -> !date.isAfter(yesterday) }
            .forEach { date -> aggregateCompletedDay(date) }
    }

    @Transactional
    fun aggregateCompletedDay(metricDate: LocalDate = LocalDate.now().minusDays(1)) {
        val from = metricDate.atStartOfDay()
        val to = metricDate.plusDays(1).atStartOfDay()

        val dailyActiveUsers = userActivityEventRepository.countDistinctApplicantsByEventTypeBetween(
            ActivityEventType.LOGIN,
            from,
            to
        )
        val dailySignups = userActivityEventRepository.countEventsByEventTypeBetween(
            ActivityEventType.SIGNUP,
            from,
            to
        )

        upsert(metricDate, PlatformMetricName.DAILY_ACTIVE_USERS, dailyActiveUsers)
        upsert(metricDate, PlatformMetricName.DAILY_SIGNUPS, dailySignups)

        logger.info {
            "Aggregated platform activity for $metricDate: dailyActiveUsers=$dailyActiveUsers, dailySignups=$dailySignups"
        }
    }

    @Transactional
    fun purgeRawEvents(retentionDays: Long = 90): Int {
        val cutoff = LocalDateTime.now().minusDays(retentionDays)
        val deleted = userActivityEventRepository.deleteByTimestampBefore(cutoff)
        logger.info { "Purged $deleted raw platform activity events before $cutoff" }
        return deleted
    }

    private fun getNextDateToAggregate(): LocalDate? {
        val lastAggregatedDate = platformDailyMetricRepository
            .findFirstByMetricNameOrderByMetricDateDesc(PlatformMetricName.DAILY_ACTIVE_USERS.name)
            ?.metricDate
        if (lastAggregatedDate != null) {
            return lastAggregatedDate.plusDays(1)
        }

        return userActivityEventRepository.findOldestEventTimestamp()?.toLocalDate()
    }

    private fun upsert(metricDate: LocalDate, metricName: PlatformMetricName, metricValue: Long) {
        val metricId = PlatformDailyMetricId(metricDate, metricName.name)
        val metric = platformDailyMetricRepository.findById(metricId).orElse(
            PlatformDailyMetricEntity(
                metricDate = metricDate,
                metricName = metricName.name,
                metricValue = metricValue
            )
        )

        metric.metricValue = metricValue
        metric.updatedAt = LocalDateTime.now()
        platformDailyMetricRepository.save(metric)
    }
}

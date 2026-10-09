package ch.streckeisen.mycv.backend.admin.dashboard

import ch.streckeisen.mycv.backend.admin.activity.ActivityEventType
import ch.streckeisen.mycv.backend.admin.activity.UserActivityEventRepository
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

class PlatformActivityAggregationServiceTest {
    private lateinit var userActivityEventRepository: UserActivityEventRepository
    private lateinit var platformDailyMetricRepository: PlatformDailyMetricRepository
    private lateinit var aggregationService: PlatformActivityAggregationService

    @BeforeEach
    fun setup() {
        userActivityEventRepository = mockk {
            every {
                countDistinctApplicantsByEventTypeBetween(
                    eq(ActivityEventType.LOGIN),
                    any(),
                    any()
                )
            } returns 7
            every {
                countEventsByEventTypeBetween(
                    eq(ActivityEventType.SIGNUP),
                    any(),
                    any()
                )
            } returns 3
            every { findOldestEventTimestamp() } returns null
            every { deleteByTimestampBefore(any()) } returns 12
        }
        platformDailyMetricRepository = mockk {
            every { findById(any()) } returns Optional.empty()
            every { findFirstByMetricNameOrderByMetricDateDesc(any()) } returns null
            every { save(any<PlatformDailyMetricEntity>()) } answers { firstArg() }
        }
        aggregationService = PlatformActivityAggregationService(
            userActivityEventRepository,
            platformDailyMetricRepository
        )
    }

    @Test
    fun testAggregateCompletedDayUpsertsDailyMetrics() {
        val metricDate = LocalDate.of(2026, 8, 14)

        aggregationService.aggregateCompletedDay(metricDate)

        val expectedFrom = LocalDateTime.of(2026, 8, 14, 0, 0)
        val expectedTo = LocalDateTime.of(2026, 8, 15, 0, 0)
        verify(exactly = 1) {
            userActivityEventRepository.countDistinctApplicantsByEventTypeBetween(
                ActivityEventType.LOGIN,
                expectedFrom,
                expectedTo
            )
        }
        verify(exactly = 1) {
            userActivityEventRepository.countEventsByEventTypeBetween(
                ActivityEventType.SIGNUP,
                expectedFrom,
                expectedTo
            )
        }
        verify(exactly = 1) {
            platformDailyMetricRepository.save(
                match {
                    it.metricDate == metricDate &&
                        it.metricName == PlatformMetricName.DAILY_ACTIVE_USERS.name &&
                        it.metricValue == 7L
                }
            )
        }
        verify(exactly = 1) {
            platformDailyMetricRepository.save(
                match {
                    it.metricDate == metricDate &&
                        it.metricName == PlatformMetricName.DAILY_SIGNUPS.name &&
                        it.metricValue == 3L
                }
            )
        }
    }

    @Test
    fun testAggregateCompletedDayUpdatesExistingMetric() {
        val metricDate = LocalDate.of(2026, 8, 14)
        val existingActiveUserMetric = PlatformDailyMetricEntity(
            metricDate = metricDate,
            metricName = PlatformMetricName.DAILY_ACTIVE_USERS.name,
            metricValue = 1
        )
        every {
            platformDailyMetricRepository.findById(
                PlatformDailyMetricId(metricDate, PlatformMetricName.DAILY_ACTIVE_USERS.name)
            )
        } returns Optional.of(existingActiveUserMetric)

        aggregationService.aggregateCompletedDay(metricDate)

        assertEquals(7, existingActiveUserMetric.metricValue)
        verify(exactly = 1) { platformDailyMetricRepository.save(eq(existingActiveUserMetric)) }
    }

    @Test
    fun testPurgeRawEventsUsesRetentionWindow() {
        val cutoffSlot: CapturingSlot<LocalDateTime> = slot()
        val before = LocalDateTime.now().minusDays(90)

        val deleted = aggregationService.purgeRawEvents(90)

        assertEquals(12, deleted)
        verify(exactly = 1) { userActivityEventRepository.deleteByTimestampBefore(capture(cutoffSlot)) }
        val cutoff = cutoffSlot.captured
        assertTrue(!cutoff.isBefore(before))
        assertTrue(ChronoUnit.SECONDS.between(before, cutoff) < 5)
    }

    @Test
    fun testAggregateMissingCompletedDaysBackfillsFromLastAggregatedDate() {
        every {
            platformDailyMetricRepository.findFirstByMetricNameOrderByMetricDateDesc(
                PlatformMetricName.DAILY_ACTIVE_USERS.name
            )
        } returns PlatformDailyMetricEntity(
            metricDate = LocalDate.of(2026, 8, 11),
            metricName = PlatformMetricName.DAILY_ACTIVE_USERS.name,
            metricValue = 9
        )

        aggregationService.aggregateMissingCompletedDays(LocalDate.of(2026, 8, 15))

        verify(exactly = 1) {
            userActivityEventRepository.countDistinctApplicantsByEventTypeBetween(
                ActivityEventType.LOGIN,
                LocalDateTime.of(2026, 8, 12, 0, 0),
                LocalDateTime.of(2026, 8, 13, 0, 0)
            )
        }
        verify(exactly = 1) {
            userActivityEventRepository.countDistinctApplicantsByEventTypeBetween(
                ActivityEventType.LOGIN,
                LocalDateTime.of(2026, 8, 13, 0, 0),
                LocalDateTime.of(2026, 8, 14, 0, 0)
            )
        }
        verify(exactly = 1) {
            userActivityEventRepository.countDistinctApplicantsByEventTypeBetween(
                ActivityEventType.LOGIN,
                LocalDateTime.of(2026, 8, 14, 0, 0),
                LocalDateTime.of(2026, 8, 15, 0, 0)
            )
        }
    }

    @Test
    fun testAggregateMissingCompletedDaysStartsAtOldestEventWhenNoMetricsExist() {
        every { userActivityEventRepository.findOldestEventTimestamp() } returns LocalDateTime.of(2026, 8, 13, 10, 30)

        aggregationService.aggregateMissingCompletedDays(LocalDate.of(2026, 8, 15))

        verify(exactly = 1) {
            userActivityEventRepository.countEventsByEventTypeBetween(
                ActivityEventType.SIGNUP,
                LocalDateTime.of(2026, 8, 13, 0, 0),
                LocalDateTime.of(2026, 8, 14, 0, 0)
            )
        }
        verify(exactly = 1) {
            userActivityEventRepository.countEventsByEventTypeBetween(
                ActivityEventType.SIGNUP,
                LocalDateTime.of(2026, 8, 14, 0, 0),
                LocalDateTime.of(2026, 8, 15, 0, 0)
            )
        }
    }
}

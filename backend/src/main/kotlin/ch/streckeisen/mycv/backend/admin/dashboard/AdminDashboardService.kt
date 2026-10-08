package ch.streckeisen.mycv.backend.admin.dashboard

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.admin.activity.ActivityEventType
import ch.streckeisen.mycv.backend.admin.activity.UserActivityEventRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AdminDashboardService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val userActivityEventRepository: UserActivityEventRepository,
    private val platformDailyMetricRepository: PlatformDailyMetricRepository
) {
    fun getDashboard(range: String): AdminDashboardDto {
        val rangeDays = parseRangeDays(range)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val todayStart = today.atStartOfDay()
        val seriesEnd = today.minusDays(1)
        val seriesStart = seriesEnd.minusDays(rangeDays - 1)

        val metrics = platformDailyMetricRepository.findByMetricNameInAndMetricDateBetweenOrderByMetricDateAsc(
            listOf(PlatformMetricName.DAILY_ACTIVE_USERS.name, PlatformMetricName.DAILY_SIGNUPS.name),
            seriesStart,
            seriesEnd
        )
        val groupedMetrics = metrics.groupBy { it.metricName }
        val lastAggregatedAt = metrics.maxOfOrNull { it.updatedAt } ?: now

        return AdminDashboardDto(
            totalRegisteredUsers = applicantAccountRepository.count(),
            dailyActiveUsers = userActivityEventRepository.countDistinctApplicantsByEventTypeSince(
                ActivityEventType.LOGIN,
                todayStart
            ),
            weeklyActiveUsers = userActivityEventRepository.countDistinctApplicantsByEventTypeSince(
                ActivityEventType.LOGIN,
                today.minusDays(6).atStartOfDay()
            ),
            monthlyActiveUsers = userActivityEventRepository.countDistinctApplicantsByEventTypeSince(
                ActivityEventType.LOGIN,
                today.minusDays(29).atStartOfDay()
            ),
            signupSeries = buildSeries(
                seriesStart,
                seriesEnd,
                groupedMetrics[PlatformMetricName.DAILY_SIGNUPS.name].orEmpty()
            ),
            activeUserSeries = buildSeries(
                seriesStart,
                seriesEnd,
                groupedMetrics[PlatformMetricName.DAILY_ACTIVE_USERS.name].orEmpty()
            ),
            verifiedAccountCount = applicantAccountRepository.countByIsVerified(true),
            unverifiedAccountCount = applicantAccountRepository.countUnverifiedCompleteAccounts(),
            oauthSignupCount = applicantAccountRepository.countByIsOAuthUser(true),
            passwordSignupCount = applicantAccountRepository.countByIsOAuthUser(false),
            asOf = lastAggregatedAt
        )
    }

    private fun parseRangeDays(range: String): Long {
        val requestedDays = range.removeSuffix("d").toLongOrNull() ?: 30
        return requestedDays.coerceIn(1, 90)
    }

    private fun buildSeries(
        start: LocalDate,
        end: LocalDate,
        metrics: List<PlatformDailyMetricEntity>
    ): List<AdminDashboardMetricPointDto> {
        val valuesByDate = metrics.associate { it.metricDate to it.metricValue }
        return generateSequence(start) { date -> date.plusDays(1) }
            .takeWhile { date -> !date.isAfter(end) }
            .map { date -> AdminDashboardMetricPointDto(date, valuesByDate[date] ?: 0) }
            .toList()
    }
}

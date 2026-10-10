package ch.streckeisen.mycv.backend.admin.dashboard

import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.admin.activity.ActivityEventType
import ch.streckeisen.mycv.backend.admin.activity.UserActivityEventRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class AdminDashboardServiceTest {
    private lateinit var applicantAccountRepository: ApplicantAccountRepository
    private lateinit var userActivityEventRepository: UserActivityEventRepository
    private lateinit var platformDailyMetricRepository: PlatformDailyMetricRepository
    private lateinit var adminDashboardService: AdminDashboardService

    @BeforeEach
    fun setup() {
        applicantAccountRepository = mockk {
            every { count() } returns 42
            every { countByIsVerified(true) } returns 32
            every { countUnverifiedCompleteAccounts() } returns 10
            every { countByIsOAuthUser(true) } returns 8
            every { countByIsOAuthUser(false) } returns 34
        }
        userActivityEventRepository = mockk {
            every {
                countDistinctApplicantsByEventTypeSince(eq(ActivityEventType.LOGIN), any())
            } returnsMany listOf(4, 9, 20)
        }
        platformDailyMetricRepository = mockk {
            every {
                findByMetricNameInAndMetricDateBetweenOrderByMetricDateAsc(any(), any(), any())
            } returns listOf(
                PlatformDailyMetricEntity(
                    metricDate = LocalDate.now().minusDays(1),
                    metricName = PlatformMetricName.DAILY_ACTIVE_USERS.name,
                    metricValue = 4,
                    updatedAt = LocalDateTime.of(2026, 8, 14, 4, 0)
                ),
                PlatformDailyMetricEntity(
                    metricDate = LocalDate.now().minusDays(1),
                    metricName = PlatformMetricName.DAILY_SIGNUPS.name,
                    metricValue = 2,
                    updatedAt = LocalDateTime.of(2026, 8, 14, 3, 0)
                )
            )
        }
        adminDashboardService = AdminDashboardService(
            applicantAccountRepository,
            userActivityEventRepository,
            platformDailyMetricRepository
        )
    }

    @Test
    fun testGetDashboardBuildsSummaryAndSeries() {
        val dashboard = adminDashboardService.getDashboard("3d")

        assertEquals(42, dashboard.totalRegisteredUsers)
        assertEquals(4, dashboard.dailyActiveUsers)
        assertEquals(9, dashboard.weeklyActiveUsers)
        assertEquals(20, dashboard.monthlyActiveUsers)
        assertEquals(32, dashboard.verifiedAccountCount)
        assertEquals(10, dashboard.unverifiedAccountCount)
        assertEquals(8, dashboard.oauthSignupCount)
        assertEquals(34, dashboard.passwordSignupCount)
        assertEquals(LocalDateTime.of(2026, 8, 14, 4, 0), dashboard.asOf)
        assertEquals(3, dashboard.activeUserSeries.size)
        assertEquals(3, dashboard.signupSeries.size)
        assertEquals(4, dashboard.activeUserSeries[2].value)
        assertEquals(2, dashboard.signupSeries[2].value)
        assertEquals(0, dashboard.activeUserSeries.first().value)
    }

    @Test
    fun testGetDashboardClampsRangeToNinetyDays() {
        val dashboard = adminDashboardService.getDashboard("900d")

        assertEquals(90, dashboard.activeUserSeries.size)
        assertEquals(90, dashboard.signupSeries.size)
    }
}

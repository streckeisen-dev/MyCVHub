package ch.streckeisen.mycv.backend.admin.dashboard

import java.time.LocalDate
import java.time.OffsetDateTime

data class AdminDashboardDto(
    val totalRegisteredUsers: Long,
    val dailyActiveUsers: Long,
    val weeklyActiveUsers: Long,
    val monthlyActiveUsers: Long,
    val signupSeries: List<AdminDashboardMetricPointDto>,
    val activeUserSeries: List<AdminDashboardMetricPointDto>,
    val verifiedAccountCount: Long,
    val unverifiedAccountCount: Long,
    val oauthSignupCount: Long,
    val passwordSignupCount: Long,
    val asOf: OffsetDateTime
)

data class AdminDashboardMetricPointDto(
    val date: LocalDate,
    val value: Long
)

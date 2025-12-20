package ch.streckeisen.mycv.backend.dashboard

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.application.ApplicationService
import ch.streckeisen.mycv.backend.application.dto.toDto
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.annotations.RequiresAccountStatus
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardResource(
    private val profileService: ProfileService,
    private val applicationService: ApplicationService,
    private val messagesService: MessagesService
) {

    @GetMapping
    @RequiresAccountStatus(AccountStatus.UNVERIFIED)
    fun getDashboardInfo(): ResponseEntity<DashboardInfoDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        val isVerified = principal.status.permissionValue > AccountStatus.UNVERIFIED.permissionValue
        val profileInfo = profileService.getProfileStats(principal.id)
            .fold(
                onSuccess = { stats ->
                    ProfileInfoDto(
                        experienceCount = stats.experienceCount,
                        educationCount = stats.educationCount,
                        projectCount = stats.projectCount,
                        skillCount = stats.skillCount
                    )
                },
                onFailure = {
                    if (it is LocalizedException) {
                        null
                    } else {
                        throw it
                    }
                }
            )

        val applicationStats = applicationService.getApplicationStats(principal.id)
            .fold(
                onSuccess = { stats ->
                    stats.map { stat -> ApplicationInfoDto(stat.status.toDto(messagesService), stat.count) }
                },
                onFailure = { throw it }
            )

        val dashboardInfo = DashboardInfoDto(
            isVerified = isVerified,
            profile = profileInfo,
            applications = applicationStats
        )
        return ResponseEntity.ok(dashboardInfo)
    }
}
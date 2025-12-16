package ch.streckeisen.mycv.backend.dashboard

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
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
    private val profileService: ProfileService
) {

    @GetMapping
    @RequiresAccountStatus(AccountStatus.UNVERIFIED)
    fun getDashboardInfo(): ResponseEntity<DashboardInfoDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        val isVerified = principal.status.permissionValue > AccountStatus.UNVERIFIED.permissionValue
        return profileService.getProfileStats(principal.id)
            .fold(
                onSuccess = { stats ->
                    ResponseEntity.ok(
                        DashboardInfoDto(
                            isVerified = isVerified,
                            profile = ProfileInfoDto(
                                experienceCount = stats.experienceCount,
                                educationCount = stats.educationCount,
                                projectCount = stats.projectCount,
                                skillCount = stats.skillCount
                            )
                        )
                    )
                },
                onFailure = {
                    if (it is LocalizedException) {
                        ResponseEntity.ok(
                            DashboardInfoDto(
                                isVerified = isVerified,
                                profile = null
                            )
                        )
                    } else {
                        throw it
                    }
                }
            )
    }
}
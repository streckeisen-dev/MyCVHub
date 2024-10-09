package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/profile")
class PublicProfileResource(
    private val profileService: ProfileService
) {
    @GetMapping("/{alias}")
    fun getApplicant(@PathVariable("alias") alias: String): ResponseEntity<PublicProfileDto> {
        return profileService.findByAlias(alias)
            .fold(
                onSuccess = { profile ->
                    val principal = SecurityContextHolder.getContext().authentication?.principal as MyCvPrincipal
                    if (profile.isProfilePublic || (profile.account.id == principal.id)) {
                        val publicProfile = profile.toPublicDto()
                        return@fold ResponseEntity.ok(publicProfile)
                    }
                    throw AccessDeniedException("You don't have permission to view this profile")
                },
                onFailure = {
                    throw ResultNotFoundException("The profile doesn't exist or is private")
                }
            )
    }
}
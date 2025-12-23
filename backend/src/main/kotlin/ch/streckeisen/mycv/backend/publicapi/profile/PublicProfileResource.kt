package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import ch.streckeisen.mycv.backend.security.getMyCvPrincipalOrNull
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@PublicApi
@RestController
@RequestMapping("/api/public/profile")
class PublicProfileResource(
    private val profileService: ProfileService,
    private val profilePictureService: ProfilePictureService
) {
    @GetMapping("/{username}")
    fun getApplicant(@PathVariable username: String): ResponseEntity<PublicProfileDto> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipalOrNull()

        return profileService.findByUsername(principal?.id, username)
            .fold(
                onSuccess = { profile ->
                    val profilePicture = profilePictureService.get(profile.account.id, profile)
                        .getOrThrow()
                    ResponseEntity.ok(profile.toPublicDto(profilePicture.uri.toString()))
                },
                onFailure = {
                    throw it
                }
            )
    }
}
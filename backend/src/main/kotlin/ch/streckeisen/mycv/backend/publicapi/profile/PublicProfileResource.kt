package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.security.getMyCvPrincipalOrNull
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/profile")
class PublicProfileResource(
    private val profileService: ProfileService,
    private val profilePictureService: ProfilePictureService
) {
    @GetMapping("/{alias}")
    fun getApplicant(@PathVariable("alias") alias: String): ResponseEntity<PublicProfileDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipalOrNull()

        return profileService.findByAlias(principal?.id, alias)
            .fold(
                onSuccess = { profile ->
                    ResponseEntity.ok(profile.toPublicDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @GetMapping("/{alias}/picture")
    fun getPicture(@PathVariable("alias") alias: String): ResponseEntity<Resource> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipalOrNull()

        val profile = profileService.findByAlias(principal?.id, alias)
            .getOrThrow()

        return profilePictureService.get(principal?.id, profile)
            .fold(
                onSuccess = { profilePicture ->
                    val resource = UrlResource(profilePicture.uri)
                    ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${profilePicture.name}\"")
                        .body(resource)
                },
                onFailure = {
                    throw it
                }
            )
    }
}
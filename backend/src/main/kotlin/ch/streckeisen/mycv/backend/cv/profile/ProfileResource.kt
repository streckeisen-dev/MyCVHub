package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileResource(
    private val profileService: ProfileService
) {
    @GetMapping
    fun getProfile(): ResponseEntity<ProfileDto> {
        val principal = SecurityContextHolder.getContext().authentication.principal as MyCvPrincipal

        return profileService.findByAccountId(principal.id)
            .fold(
                onSuccess = { profile ->
                    ResponseEntity.ok(profile.toDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @PostMapping
    fun updateGeneralInformation(@RequestBody profileInformationUpdate: GeneralProfileInformationUpdateDto): ResponseEntity<ProfileDto> {
        val principal = SecurityContextHolder.getContext().authentication.principal as MyCvPrincipal
        return profileService.updateGeneralInformation(principal.id, profileInformationUpdate)
            .fold(
                onSuccess = { profile ->
                    ResponseEntity.ok(profile.toDto())
                },
                onFailure = {
                    throw it
                }
            )
    }
}
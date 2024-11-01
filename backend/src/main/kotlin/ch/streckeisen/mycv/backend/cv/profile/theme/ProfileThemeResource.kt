package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile/theme")
class ProfileThemeResource(
    private val profileThemeService: ProfileThemeService
) {
    @PostMapping
    fun saveProfileTheme(@RequestBody profileThemeUpdate: ProfileThemeUpdateDto): ResponseEntity<ProfileThemeDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return profileThemeService.save(principal.id, profileThemeUpdate).fold(
            onSuccess = { profileTheme ->
                ResponseEntity.ok(profileTheme.toDto())
            },
            onFailure = {
                throw it
            }
        )
    }
}
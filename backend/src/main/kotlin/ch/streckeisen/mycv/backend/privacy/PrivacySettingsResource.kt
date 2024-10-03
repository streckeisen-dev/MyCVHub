package ch.streckeisen.mycv.backend.privacy

import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/privacy")
class PrivacySettingsResource(
    private val privacySettingsService: PrivacySettingsService
) {
    @GetMapping
    fun loadPrivacySettings(): ResponseEntity<PrivacySettingsUpdateDto> {
        val principal = SecurityContextHolder.getContext().authentication.principal as MyCvPrincipal
        val privacySettings = privacySettingsService.findByApplicant(principal.id)!!
        return ResponseEntity.ok(PrivacySettingsUpdateDto(
            privacySettings.isProfilePublic,
            privacySettings.isEmailPublic,
            privacySettings.isPhonePublic,
            privacySettings.isBirthdayPublic,
            privacySettings.isAddressPublic
        ))
    }

    @PostMapping
    fun updatePrivacySettings(@RequestBody privacySettingsUpdateDto: PrivacySettingsUpdateDto): ResponseEntity<Unit> {
        val auth: Authentication? = SecurityContextHolder.getContext().authentication
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val principal = auth.principal as MyCvPrincipal

        privacySettingsService.update(privacySettingsUpdateDto, principal.id)
            .fold(
                onSuccess = { return ResponseEntity.ok().build() },
                onFailure = { throw it }
            )
    }
}
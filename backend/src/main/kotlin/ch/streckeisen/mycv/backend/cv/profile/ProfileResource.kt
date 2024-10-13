package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/profile")
class ProfileResource(
    private val profileService: ProfileService,
    private val objectMapper: ObjectMapper
) {
    @GetMapping
    fun getProfile(): ResponseEntity<ProfileDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

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

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateGeneralInformation(
        @RequestPart("profilePicture", required = false) profilePicture: MultipartFile?,
        @RequestPart("data", required = true) data: String
    ): ResponseEntity<ProfileDto> {
        val profileInformationUpdate = objectMapper.readValue(data, GeneralProfileInformationUpdateDto::class.java)

        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        return profileService.updateGeneralInformation(principal.id, profileInformationUpdate, profilePicture)
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
package ch.streckeisen.mycv.backend.publicapi

import ch.streckeisen.mycv.backend.cv.applicant.ApplicantService
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
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
    private val applicantService: ApplicantService
) {
    @GetMapping("/{id}")
    fun getApplicant(@PathVariable("id") id: Long): ResponseEntity<PublicProfileDto> {
        return applicantService.getById(id)
            .fold(
                onSuccess = { applicant ->
                    val currentUsername = SecurityContextHolder.getContext().authentication?.principal as String?
                    if (applicant.hasPublicProfile || (applicant.email == currentUsername)) {
                        return@fold ResponseEntity.ok(applicant.toPublicDto())
                    }
                    throw AccessDeniedException("You don't have permission to view this profile")
                },
                onFailure = {
                    throw ResultNotFoundException("Profile not found")
                }
            )
    }

    @GetMapping("/{alias}")
    fun getApplicantByAlias(@PathVariable("alias") alias: String): ResponseEntity<PublicProfileDto> {
        return applicantService.getById(2)
            .fold(
                onSuccess = { applicant ->
                    val currentUsername = SecurityContextHolder.getContext().authentication?.principal as String?
                    if (applicant.hasPublicProfile || (applicant.email == currentUsername)) {
                        return@fold ResponseEntity.ok(applicant.toPublicDto())
                    }
                    throw AccessDeniedException("You don't have permission to view this profile")
                },
                onFailure = {
                    throw ResultNotFoundException("Profile not found")
                }
            )
    }
}
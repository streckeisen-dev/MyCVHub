package ch.streckeisen.mycv.backend.cv.applicant.publicapi

import ch.streckeisen.mycv.backend.cv.applicant.ApplicantService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public")
class ApplicantResource(
    private val applicantService: ApplicantService
) {
    @GetMapping("/{id}")
    fun getApplicant(@PathVariable("id") id: Long): ResponseEntity<PublicApplicantDto> {
        return applicantService.getById(id)
            .fold(
                onSuccess = {
                    ResponseEntity.ok(it)
                },
                onFailure = {
                    ResponseEntity.notFound().build()
                }
            )
    }
}
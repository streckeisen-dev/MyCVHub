package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile/education")
class EducationResource(
    private val educationService: EducationService
) {
    @PostMapping
    fun saveEducation(@RequestBody educationUpdate: EducationUpdateDto): ResponseEntity<EducationDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return educationService.save(principal.id, educationUpdate)
            .fold(
                onSuccess = { education ->
                    ResponseEntity.ok(education.toDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @DeleteMapping("{id}")
    fun deleteEducation(@PathVariable("id") id: Long): ResponseEntity<Void> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return educationService.delete(principal.id, id)
            .fold(
                onSuccess = {
                    ResponseEntity.ok().build()
                },
                onFailure = {
                    throw it
                }
            )
    }
}
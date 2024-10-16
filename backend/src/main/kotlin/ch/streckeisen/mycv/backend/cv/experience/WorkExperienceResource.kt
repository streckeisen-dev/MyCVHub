package ch.streckeisen.mycv.backend.cv.experience

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
@RequestMapping("/api/profile/work-experience")
class WorkExperienceResource(
    private val workExperienceService: WorkExperienceService
) {
    @PostMapping
    fun saveWorkExperience(@RequestBody updateRequest: WorkExperienceUpdateDto): ResponseEntity<WorkExperienceDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return workExperienceService.save(principal.id, updateRequest)
            .fold(
                onSuccess = { workExperience ->
                    ResponseEntity.ok(workExperience.toDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @DeleteMapping("{id}")
    fun deleteWorkExperience(@PathVariable("id") id: Long): ResponseEntity<Void> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return workExperienceService.delete(principal.id, id)
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
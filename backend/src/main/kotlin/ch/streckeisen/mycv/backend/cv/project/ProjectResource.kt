package ch.streckeisen.mycv.backend.cv.project

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
@RequestMapping("/api/profile/project")
class ProjectResource(
    private val projectService: ProjectService
) {
    @PostMapping
    fun saveProject(@RequestBody updateRequest: ProjectUpdateDto): ResponseEntity<ProjectDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return projectService.save(principal.id, updateRequest)
            .fold(
                onSuccess = { project ->
                    ResponseEntity.ok(project.toDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @DeleteMapping("{id}")
    fun deleteProject(@PathVariable id: Long): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return projectService.delete(principal.id, id)
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
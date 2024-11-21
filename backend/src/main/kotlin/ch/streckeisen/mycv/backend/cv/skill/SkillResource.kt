package ch.streckeisen.mycv.backend.cv.skill

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
@RequestMapping("/api/profile/skill")
class SkillResource(
    private val skillService: SkillService
) {
    @PostMapping
    fun saveSkill(@RequestBody skillUpdate: SkillUpdateDto): ResponseEntity<SkillDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return skillService.save(principal.id, skillUpdate)
            .fold(
                onSuccess = { skill ->
                    ResponseEntity.ok(skill.toDto())
                },
                onFailure = {
                    throw it
                }
            )
    }

    @DeleteMapping("{id}")
    fun deleteSkill(@PathVariable id: Long): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return skillService.delete(principal.id, id)
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
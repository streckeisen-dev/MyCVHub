package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/application-template")
class ApplicationTemplateResource(
    private val applicationTemplateService: ApplicationTemplateService
) {

    @GetMapping
    fun getApplicationTemplates(): ResponseEntity<List<ApplicationTemplateDto>> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        val result = applicationTemplateService.findApplicationTemplates(principal.id)
        return ResponseEntity.ok(result.map { it.toDto() })
    }

    @PostMapping
    fun saveApplicationTemplate(@RequestBody updateRequest: ApplicationTemplateUpdateDto): ResponseEntity<ApplicationTemplateDto> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        return applicationTemplateService.save(principal.id, updateRequest)
            .fold(
                onSuccess = { template -> ResponseEntity.ok(template.toDto()) },
                onFailure = { throw it }
            )
    }

    @DeleteMapping("{id}")
    fun deleteApplicationTemplate(@PathVariable id: Long): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        return applicationTemplateService.delete(principal.id, id)
            .fold(
                onSuccess = { ResponseEntity.ok().build() },
                onFailure = { throw it }
            )
    }
}
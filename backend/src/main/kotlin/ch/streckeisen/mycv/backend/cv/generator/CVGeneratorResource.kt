package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cv/")
class CVGeneratorResource(
    private val cvGeneratorService: CVGeneratorService,
    private val messagesService: MessagesService
) {

    @GetMapping("styles")
    fun getCVStyles(): ResponseEntity<List<CVStyleDto>> {
        val styles = CVStyle
            .entries
            .map { style ->
                CVStyleDto(
                    key = style.cvTemplate,
                    name = messagesService.getMessage(style.nameKey),
                    description = messagesService.getMessage(style.descriptionKey),
                    options = style.options.map { option ->
                        CVStyleOptionDto(
                            key = option.key,
                            name = messagesService.getMessage(option.nameKey),
                            type = option.type,
                            default = option.defaultValue
                        )
                    }
                )
            }
        return ResponseEntity.ok(styles)
    }

    @PostMapping("generate", produces = [MediaType.APPLICATION_PDF_VALUE])
    suspend fun generate(@RequestParam("style") cvStyle: String?, @RequestBody generationRequest: CVGenerationRequestDto): ResponseEntity<ByteArray> {
        val style = CVStyle.fromTemplate(cvStyle)
        if (style == null) return ResponseEntity.badRequest().build()


        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        val file = cvGeneratorService.generateCV(
            principal.id,
            style,
            generationRequest.includedWorkExperience,
            generationRequest.includedEducation,
            generationRequest.includedProjects,
            generationRequest.includedSkills,
            generationRequest.templateOptions
        )
            .onFailure { throw it }
            .getOrThrow()
        return ResponseEntity.ok(file)
    }
}
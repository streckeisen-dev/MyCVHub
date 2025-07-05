package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
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
                    style.cvTemplate,
                    messagesService.getMessage(style.nameKey),
                    messagesService.getMessage(style.descriptionKey)
                )
            }
        return ResponseEntity.ok(styles)
    }

    @GetMapping("generate", produces = [MediaType.APPLICATION_PDF_VALUE])
    suspend fun generate(@RequestParam("style") cvStyle: String?): ResponseEntity<ByteArray> {
        val style = CVStyle.fromTemplate(cvStyle)
        if (style == null) return ResponseEntity.badRequest().build()


        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        val file = cvGeneratorService.generateCV(principal.id, style)
            .onFailure { throw it }
            .getOrThrow()
        return ResponseEntity.ok(file)
    }
}
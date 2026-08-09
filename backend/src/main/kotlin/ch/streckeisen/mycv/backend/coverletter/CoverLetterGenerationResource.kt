package ch.streckeisen.mycv.backend.coverletter

import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cover-letter")
class CoverLetterGenerationResource(
    private val coverLetterGenerationService: CoverLetterGenerationService,
    private val messagesService: MessagesService
) {
    @GetMapping("styles")
    fun getCVStyles(): ResponseEntity<List<CoverLetterStyleDto>> {
        val styles = CoverLetterStyle
            .entries
            .map { style ->
                CoverLetterStyleDto(
                    key = style.styleKey,
                    name = messagesService.getMessage(style.nameKey),
                    description = messagesService.getMessage(style.descriptionKey)
                )
            }
        return ResponseEntity.ok(styles)
    }

    @PostMapping("generate", produces = [MediaType.APPLICATION_PDF_VALUE])
    suspend fun generate(
        @AuthenticationPrincipal principal: MyCvPrincipal,
        @RequestBody generationRequest: CoverLetterGenerationRequestDto
    ): ResponseEntity<ByteArray> {
        return coverLetterGenerationService.generateCoverLetter(principal.id, generationRequest)
            .fold(
                onSuccess = { file -> ResponseEntity.ok(file) },
                onFailure = { throw it },
            )
    }
}

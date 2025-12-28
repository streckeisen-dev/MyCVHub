package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.application.dto.ApplicationDetailsDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationSearchDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationStatusDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.application.dto.toDetailsDto
import ch.streckeisen.mycv.backend.application.dto.toDto
import ch.streckeisen.mycv.backend.application.dto.toSearchDto
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

const val DEFAULT_PAGE_SIZE = 10

@RestController
@RequestMapping("/api/application")
class ApplicationResource(
    private val applicationService: ApplicationService,
    private val messagesService: MessagesService
) {
    @GetMapping("/{id}")
    fun getApplication(@PathVariable id: Long): ResponseEntity<ApplicationDetailsDto> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        val application = applicationService.findById(principal.id, id)
            .fold(
                onSuccess = { it },
                onFailure = { throw it }
            )

        return applicationService.getAvailableTransitions(application.status)
            .fold(
                onSuccess = { transitions ->
                    ResponseEntity.ok(application.toDetailsDto(transitions, messagesService))
                },
                onFailure = { throw it }
            )
    }

    @GetMapping("/search")
    fun getApplications(
        @RequestParam page: Int?,
        @RequestParam(required = false) pageSize: Int?,
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false) status: ApplicationStatus?,
        @RequestParam(required = false) includeArchived: Boolean?,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) sortDirection: String?,
    ): ResponseEntity<Page<ApplicationSearchDto>> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        return applicationService.searchApplications(
            accountId = principal.id,
            page = page ?: 0,
            pageSize = pageSize ?: DEFAULT_PAGE_SIZE,
            searchTerm = searchTerm,
            status = status,
            includeArchived = includeArchived ?: false,
            sort = sort,
            sortDirection = sortDirection
        )
            .fold(
                onSuccess = { page ->
                    ResponseEntity.ok(page.map { it.toSearchDto(messagesService) })
                },
                onFailure = { throw it }
            )
    }

    @PostMapping
    fun saveApplication(@RequestBody update: ApplicationUpdateDto): ResponseEntity<ApplicationDetailsDto> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        val application = applicationService.save(principal.id, update)
            .fold(
                onSuccess = { it },
                onFailure = { throw it }
            )

        return applicationService.getAvailableTransitions(application.status)
            .fold(
                onSuccess = { transitions ->
                    ResponseEntity.ok(
                        application.toDetailsDto(
                            transitions,
                            messagesService
                        )
                    )
                },
                onFailure = { throw it }
            )
    }

    @GetMapping("/statuses")
    fun getApplicationStatuses(): ResponseEntity<List<ApplicationStatusDto>> {
        return ResponseEntity.ok(ApplicationStatus.entries.map { it.toDto(messagesService) })
    }

    @PutMapping("/transition/{id}")
    fun transitionApplication(
        @PathVariable("id") transitionId: Int,
        @RequestBody transitionRequest: ApplicationTransitionRequestDto
    ): ResponseEntity<ApplicationDetailsDto> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        val application = applicationService.transition(principal.id, transitionId, transitionRequest)
            .fold(
                onSuccess = { it },
                onFailure = { throw it }
            )

        return applicationService.getAvailableTransitions(application.status)
            .fold(
                onSuccess = { transitions ->
                    ResponseEntity.ok(application.toDetailsDto(transitions, messagesService))
                },
                onFailure = { throw it }
            )
    }

    @PutMapping("{id}/archive")
    fun archiveApplication(@PathVariable id: Long): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        return applicationService.archive(principal.id, id)
            .fold(
                onSuccess = { ResponseEntity.ok().build() },
                onFailure = { throw it }
            )
    }

    @DeleteMapping("{id}")
    fun deleteApplication(@PathVariable id: Long): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipal()

        return applicationService.delete(principal.id, id)
            .fold(
                onSuccess = { ResponseEntity.ok().build() },
                onFailure = { throw it }
            )
    }
}
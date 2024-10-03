package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.cv.applicant.ApplicantRepository
import ch.streckeisen.mycv.backend.security.MyCvPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrElse

@RestController
@RequestMapping("/api/account")
class AccountResource(private val applicantRepository: ApplicantRepository) {
    @GetMapping
    fun getAccount(): ResponseEntity<AccountDto> {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val principal = auth.principal as MyCvPrincipal
        val account = applicantRepository.findById(principal.id)
            .map { it.toAccountDto() }
            .getOrElse {
                return ResponseEntity.internalServerError().build()
            }

        return ResponseEntity.ok(account)
    }
}
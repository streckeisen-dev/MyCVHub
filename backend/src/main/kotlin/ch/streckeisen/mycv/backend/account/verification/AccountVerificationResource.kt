package ch.streckeisen.mycv.backend.account.verification

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.security.RequiresAccountStatus
import ch.streckeisen.mycv.backend.security.PublicApi
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account/verification")
class AccountVerificationResource(
    private val accountVerificationService: AccountVerificationService
) {
    @RequiresAccountStatus(AccountStatus.UNVERIFIED)
    @PostMapping("generate")
    fun generateToken(): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        accountVerificationService.generateVerificationToken(principal.id)
            .fold(
                onSuccess = {
                    return ResponseEntity.ok().build()
                },
                onFailure = {
                    throw it
                }
            )
    }

    @PublicApi
    @PostMapping
    fun verifyAccount(@RequestBody accountVerificationDto: AccountVerificationDto): ResponseEntity<Unit> {
        if (accountVerificationDto.id == null || accountVerificationDto.token == null) {
            return ResponseEntity.badRequest().build()
        }

        accountVerificationService.verifyToken(accountVerificationDto.id, accountVerificationDto.token)
            .fold(
                onSuccess = {
                    return ResponseEntity.ok().build()
                },
                onFailure = {
                    throw it
                }
            )
    }
}
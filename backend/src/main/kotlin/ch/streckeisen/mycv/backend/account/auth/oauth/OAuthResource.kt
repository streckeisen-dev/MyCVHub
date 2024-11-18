package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.account.dto.AccountDto
import ch.streckeisen.mycv.backend.account.dto.OAuthSignupRequestDto
import ch.streckeisen.mycv.backend.account.toAccountDto
import ch.streckeisen.mycv.backend.security.annotations.RequiresAccountStatus
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oauth")
class OAuthResource(
    private val oAuthService: OAuthService
) {
    @RequiresAccountStatus(AccountStatus.INCOMPLETE, exact = true)
    @PostMapping("/signup")
    fun oauthSignup(@RequestBody oauthSignupRequest: OAuthSignupRequestDto): ResponseEntity<AccountDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        return oAuthService.completeSignup(principal.id, oauthSignupRequest)
            .fold(
                onSuccess = { account ->
                    ResponseEntity.ok(account.toAccountDto())
                },
                onFailure = {
                    throw it
                }
            )
    }
}
package ch.streckeisen.mycv.backend.account.auth.oauth

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.account.auth.AuthTokenService
import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import ch.streckeisen.mycv.backend.account.dto.OAuthSignupRequestDto
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
    private val oAuthIntegrationService: OAuthIntegrationService,
    private val authTokenService: AuthTokenService
) {
    @RequiresAccountStatus(AccountStatus.INCOMPLETE, exact = true)
    @PostMapping("/signup")
    fun oauthSignup(@RequestBody oauthSignupRequest: OAuthSignupRequestDto): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()
        val signupResult = oAuthIntegrationService.completeSignup(principal.id, oauthSignupRequest)
        return authTokenService.handleAuthTokenResult(signupResult)
    }
}
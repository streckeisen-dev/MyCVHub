package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/oauth2")
class OAuthResource(
    private val authenticationService: AuthenticationService
) {

    /*@GetMapping("/callback/github")
    fun githubCallback(@AuthenticationPrincipal user: OAuth2User): ResponseEntity<AuthResponseDto> {
        val githubId = user.attributes["id"] as String
        val username = user.attributes["login"] as String

        val authResult = authenticationService.authenticateGitHubUser(githubId, username)
        return handleAuthResult(authResult)
    }

    @GetMapping("github")
    fun loginWithGithub() {

    }*/
}
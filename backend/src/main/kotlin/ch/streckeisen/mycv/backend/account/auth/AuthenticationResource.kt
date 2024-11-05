package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import ch.streckeisen.mycv.backend.security.getMyCvPrincipalOrNull
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(("/api/auth"))
class AuthenticationResource(
    private val authenticationService: AuthenticationService
) {
    @PostMapping("/login")
    fun authenticate(@RequestBody loginRequest: LoginRequestDto): ResponseEntity<AuthResponseDto> {
        val loginResult = authenticationService.authenticate(loginRequest)
        return handleAuthResult(loginResult)
    }

    @GetMapping("/login/verify")
    fun verifyLogin(): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipalOrNull()
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/signUp")
    fun register(@RequestBody userRegistration: SignupRequestDto): ResponseEntity<AuthResponseDto> {
        val loginResult = authenticationService.signUp(userRegistration)
        return handleAuthResult(loginResult)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(request: HttpServletRequest): ResponseEntity<AuthResponseDto> {
        val refreshToken = request.cookies?.find { cookie -> cookie.name == REFRESH_TOKEN_NAME }?.value
        if (refreshToken == null) {
            throw AccessDeniedException("Refresh token is required to refresh access")
        }

        val refreshResult = authenticationService.refreshAccessToken(refreshToken)
        return handleAuthResult(refreshResult)
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody changePasswordDto: ChangePasswordDto): ResponseEntity<AuthResponseDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return authenticationService.changePassword(principal.id, changePasswordDto)
            .fold(
                onSuccess = {
                    ResponseEntity.ok().build()
                },
                onFailure = {
                    throw it
                }
            )
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, createRefreshCookie("", 0).toString())
        headers.add(HttpHeaders.SET_COOKIE, createAccessCookie("", 0).toString())
        headers.add(
            HttpHeaders.SET_COOKIE, ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build()
                .toString()
        )
        return ResponseEntity.ok()
            .headers(headers)
            .build()
    }
}
package ch.streckeisen.mycv.backend.account

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REFRESH_TOKEN_NAME = "refreshToken"

@RestController
@RequestMapping(("/api/auth"))
class AuthenticationResource(
    private val authenticationService: AuthenticationService
) {
    @PostMapping("/login")
    fun authenticate(@RequestBody loginRequest: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        val loginResult = authenticationService.authenticate(loginRequest)
        return handleLoginResult(loginResult)
    }

    @PostMapping("/signUp")
    fun register(@RequestBody userRegistration: UserRegistrationDto): ResponseEntity<LoginResponseDto> {
        val loginResult = authenticationService.registerNewUser(userRegistration)
        return handleLoginResult(loginResult)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponseDto> {
        val refreshToken = request.cookies?.find { cookie -> cookie.name == REFRESH_TOKEN_NAME }?.value
        if (refreshToken == null) {
            throw AccessDeniedException("Refresh token is required to refresh access")
        }

        val refreshResult = authenticationService.refreshAccessToken(refreshToken)
        return handleLoginResult(refreshResult)
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, createRefreshToken("", 0).toString())
        return ResponseEntity.ok()
            .headers(headers)
            .build()
    }

    private fun handleLoginResult(loginResult: Result<AuthData>): ResponseEntity<LoginResponseDto> {
        return loginResult.fold(
            onSuccess = { authData ->
                val refreshCookie =
                    createRefreshToken(authData.refreshToken, authData.refreshTokenExpirationTime / 1000)

                val headers = HttpHeaders()
                headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                ResponseEntity.ok()
                    .headers(headers)
                    .body(LoginResponseDto(authData.accessToken, authData.accessTokenExpirationTime))
            },
            onFailure = {
                throw it
            }
        )
    }

    private fun createRefreshToken(refreshToken: String, expiresIn: Long) =
        ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/api/auth/refresh")
            .maxAge(expiresIn)
            .sameSite("Strict")
            .build()
}
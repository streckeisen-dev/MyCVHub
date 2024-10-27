package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.LoginResponseDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
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
const val ACCESS_TOKEN_NAME = "accessToken"

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
    fun register(@RequestBody userRegistration: SignupRequestDto): ResponseEntity<LoginResponseDto> {
        val loginResult = authenticationService.signUp(userRegistration)
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
        headers.add(HttpHeaders.SET_COOKIE, createRefreshCookie("", 0).toString())
        headers.add(HttpHeaders.SET_COOKIE, createAccessCookie("", 0).toString())
        return ResponseEntity.ok()
            .headers(headers)
            .build()
    }

    private fun handleLoginResult(loginResult: Result<AuthData>): ResponseEntity<LoginResponseDto> {
        return loginResult.fold(
            onSuccess = { authData ->
                val refreshCookie =
                    createRefreshCookie(authData.refreshToken, authData.refreshTokenExpirationTime / 1000)
                val accessCookie = createAccessCookie(authData.accessToken, authData.accessTokenExpirationTime / 1000)

                val headers = HttpHeaders()
                headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString())
                ResponseEntity.ok()
                    .headers(headers)
                    .body(LoginResponseDto(authData.accessToken, authData.accessTokenExpirationTime))
            },
            onFailure = {
                throw it
            }
        )
    }

    private fun createRefreshCookie(refreshToken: String, expiresIn: Long) =
        createCookie(REFRESH_TOKEN_NAME, refreshToken, "/api/auth/refresh", expiresIn)

    private fun createAccessCookie(accessToken: String, expiresIn: Long) =
        createCookie(ACCESS_TOKEN_NAME, accessToken, "/", expiresIn)

    private fun createCookie(name: String, value: String, path: String, expiresIn: Long) =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path(path)
            .maxAge(expiresIn)
            .sameSite("Strict")
            .build()
}
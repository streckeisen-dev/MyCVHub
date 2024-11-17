package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.security.PublicApi
import ch.streckeisen.mycv.backend.security.RequiresAccountStatus
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import ch.streckeisen.mycv.backend.security.getMyCvPrincipalOrNull
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.rememberme.InvalidCookieException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(("/api/auth"))
class AuthenticationResource(
    private val authenticationService: AuthenticationService,
    private val authTokenService: AuthTokenService,
) {
    @PublicApi
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequestDto): ResponseEntity<AuthResponseDto> {
        val loginResult = authenticationService.authenticate(loginRequest)
        return handleAuthResult(loginResult)
    }

    @RequiresAccountStatus(AccountStatus.INCOMPLETE)
    @GetMapping("/login/verify")
    fun verifyLogin(): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipalOrNull()
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        return ResponseEntity.ok().build()
    }

    @PublicApi
    @PostMapping("/signup")
    fun signup(@RequestBody userRegistration: SignupRequestDto): ResponseEntity<AuthResponseDto> {
        val loginResult = authenticationService.signUp(userRegistration)
        return handleAuthResult(loginResult)
    }

    @PublicApi
    @PostMapping("/refresh")
    fun refreshAccessToken(request: HttpServletRequest): ResponseEntity<AuthResponseDto> {
        val refreshToken = request.cookies?.find { cookie -> cookie.name == REFRESH_TOKEN_NAME }?.value
        if (refreshToken == null) {
            throw InvalidCookieException("${MYCV_KEY_PREFIX}.auth.tokenRefresh.refreshTokenRequired")
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

    @PublicApi
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, authTokenService.createRefreshCookie("", 0).toString())
        headers.add(HttpHeaders.SET_COOKIE, authTokenService.createAccessCookie("", 0).toString())
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

    private fun handleAuthResult(loginResult: Result<AuthTokens>): ResponseEntity<AuthResponseDto> {
        return loginResult.fold(
            onSuccess = { authData ->
                val refreshCookie =
                    authTokenService.createRefreshCookie(
                        authData.refreshToken,
                        authData.refreshTokenExpirationTime / 1000
                    )
                val accessCookie =
                    authTokenService.createAccessCookie(authData.accessToken, authData.accessTokenExpirationTime / 1000)

                val headers = HttpHeaders()
                headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString())
                ResponseEntity.ok()
                    .headers(headers)
                    .body(AuthResponseDto(authData.accessToken, authData.accessTokenExpirationTime))
            },
            onFailure = {
                throw it
            }
        )
    }
}
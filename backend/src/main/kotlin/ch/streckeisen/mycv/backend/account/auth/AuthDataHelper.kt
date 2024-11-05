package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity

const val REFRESH_TOKEN_NAME = "refreshToken"
const val ACCESS_TOKEN_NAME = "accessToken"

fun handleAuthResult(loginResult: Result<AuthData>): ResponseEntity<AuthResponseDto> {
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
                .body(AuthResponseDto(authData.accessToken, authData.accessTokenExpirationTime))
        },
        onFailure = {
            throw it
        }
    )
}

fun createRefreshCookie(refreshToken: String, expiresIn: Long) =
    createCookie(REFRESH_TOKEN_NAME, refreshToken, "/api/auth/refresh", expiresIn)

fun createAccessCookie(accessToken: String, expiresIn: Long) =
    createCookie(ACCESS_TOKEN_NAME, accessToken, "/", expiresIn)

private fun createCookie(name: String, value: String, path: String, expiresIn: Long) =
    ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .path(path)
        .maxAge(expiresIn)
        .sameSite("Strict")
        .build()
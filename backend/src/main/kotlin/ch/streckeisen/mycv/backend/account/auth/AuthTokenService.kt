package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.UserDetailsServiceImpl
import io.jsonwebtoken.JwtException
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

const val REFRESH_TOKEN_NAME = "refreshToken"
const val ACCESS_TOKEN_NAME = "accessToken"

@Service
class AuthTokenService(
    private val userDetailsService: UserDetailsServiceImpl,
    private val jwtService: JwtService
) {
    fun generateAuthData(username: String): Result<AuthTokens> {
        val userDetails = userDetailsService.loadUserByUsernameAsResult(username)
            .getOrElse { return Result.failure(it) }
        val accessToken = jwtService.generateAccessToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        val accessTokenExpirationTime = jwtService.getAccessTokenExpirationTime()
        val refreshTokenExpirationTime = jwtService.getRefreshTokenExpirationTime()
        return Result.success(
            AuthTokens(
                accessToken,
                accessTokenExpirationTime,
                refreshToken,
                refreshTokenExpirationTime,
                userDetails.account.accountDetails?.language
            )
        )
    }

    fun validateRefreshToken(refreshToken: String): Result<String> {
        val username = jwtService.extractUsername(refreshToken)
        val userDetails = userDetailsService.loadUserByUsernameAsResult(username)
            .getOrElse { return Result.failure(it) }
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            return Result.failure(JwtException("Invalid refresh token"))
        }
        return Result.success(username!!)
    }

    fun createRefreshCookie(refreshToken: String, expiresIn: Long) =
        createCookie(REFRESH_TOKEN_NAME, refreshToken, "/api/auth/refresh", expiresIn)

    fun createAccessCookie(accessToken: String, expiresIn: Long) =
        createCookie(ACCESS_TOKEN_NAME, accessToken, "/", expiresIn)

    fun handleAuthTokenResult(authTokens: Result<AuthTokens>): ResponseEntity<AuthResponseDto> {
        return authTokens.fold(
            onSuccess = { authData ->
                val refreshCookie =
                    createRefreshCookie(
                        authData.refreshToken,
                        authData.refreshTokenExpirationTime / 1000
                    )
                val accessCookie =
                    createAccessCookie(authData.accessToken, authData.accessTokenExpirationTime / 1000)

                val headers = HttpHeaders()
                headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString())
                ResponseEntity.ok()
                    .headers(headers)
                    .body(AuthResponseDto(authData.language))
            },
            onFailure = {
                throw it
            }
        )
    }

    private fun createCookie(name: String, value: String, path: String, expiresIn: Long) =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path(path)
            .maxAge(expiresIn)
            .sameSite("Strict")
            .build()
}
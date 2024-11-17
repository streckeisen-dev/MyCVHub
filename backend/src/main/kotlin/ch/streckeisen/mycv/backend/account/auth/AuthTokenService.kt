package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.UserDetailsServiceImpl
import io.jsonwebtoken.JwtException
import org.springframework.http.ResponseCookie
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
                refreshTokenExpirationTime
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

    private fun createCookie(name: String, value: String, path: String, expiresIn: Long) =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path(path)
            .maxAge(expiresIn)
            .sameSite("Strict")
            .build()
}
package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.JwtTokenType
import io.jsonwebtoken.JwtException
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.ZoneId

const val ADMIN_REFRESH_TOKEN_NAME = "adminRefreshToken"
const val ADMIN_ACCESS_TOKEN_NAME = "adminAccessToken"

@Service
class AdminAuthTokenService(
    private val adminUserDetailsService: AdminUserDetailsService,
    private val jwtService: JwtService
) {
    fun generateAuthData(username: String): Result<AdminAuthTokens> {
        val userDetails = adminUserDetailsService.loadAdminByUsernameAsResult(username)
            .getOrElse { return Result.failure(it) }
        val accessToken = jwtService.generateAccessToken(userDetails, JwtTokenType.ADMIN)
        val refreshToken = jwtService.generateRefreshToken(userDetails, JwtTokenType.ADMIN)
        return Result.success(
            AdminAuthTokens(
                accessToken,
                jwtService.getAccessTokenExpirationTime(),
                refreshToken,
                jwtService.getRefreshTokenExpirationTime()
            )
        )
    }

    fun validateRefreshToken(refreshToken: String): Result<String> {
        val username = jwtService.extractUsernameFromRefreshToken(refreshToken)
        val userDetails = adminUserDetailsService.loadAdminByUsernameAsResult(username)
            .getOrElse { return Result.failure(it) }
        val admin = userDetails.admin
        if (admin.mustChangePassword && admin.temporaryPasswordExpiresAt?.isBefore(java.time.LocalDateTime.now()) == true) {
            return Result.failure(JwtException("Temporary admin password expired"))
        }
        val passwordChangedAt = admin.passwordChangedAt
        if (passwordChangedAt != null) {
            val issuedAt = jwtService.extractIssuedAtFromRefreshToken(refreshToken)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            if (issuedAt.isBefore(passwordChangedAt)) {
                return Result.failure(JwtException("Admin refresh token was issued before the current password"))
            }
        }
        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails, JwtTokenType.ADMIN)) {
            return Result.failure(JwtException("Invalid admin refresh token"))
        }
        return Result.success(username!!)
    }

    fun createRefreshCookie(refreshToken: String, expiresIn: Long): ResponseCookie =
        createCookie(ADMIN_REFRESH_TOKEN_NAME, refreshToken, "/api/admin/auth/refresh", expiresIn)

    fun createAccessCookie(accessToken: String, expiresIn: Long): ResponseCookie =
        createCookie(ADMIN_ACCESS_TOKEN_NAME, accessToken, "/api/admin", expiresIn)

    fun handleAuthTokenResult(authTokens: Result<AdminAuthTokens>): ResponseEntity<Unit> {
        return authTokens.fold(
            onSuccess = { authData ->
                val headers = HttpHeaders()
                headers.add(
                    HttpHeaders.SET_COOKIE,
                    createRefreshCookie(authData.refreshToken, authData.refreshTokenExpirationTime / 1000).toString()
                )
                headers.add(
                    HttpHeaders.SET_COOKIE,
                    createAccessCookie(authData.accessToken, authData.accessTokenExpirationTime / 1000).toString()
                )
                ResponseEntity.ok().headers(headers).body(Unit)
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

package ch.streckeisen.mycv.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

private const val TOKEN_TYPE_CLAIM = "tokenType"

enum class JwtTokenType {
    APPLICANT,
    ADMIN
}

@Service
class JwtService(
    @param:Value($$"${my-cv.security.jwt.access.secret}")
    private val jwtAccessSecret: String,
    @param:Value($$"${my-cv.security.jwt.refresh.secret}")
    private val jwtRefreshSecret: String,
    @param:Value($$"${my-cv.security.jwt.access.expiration-time}")
    private val jwtAccessExpirationTime: Long,
    @param:Value($$"${my-cv.security.jwt.refresh.expiration-time}")
    private val jwtRefreshExpirationTime: Long
) {
    fun extractUsernameFromAccessToken(token: String): String? {
        return extractClaim(token, getAccessSignInKey(), Claims::getSubject)
    }

    fun extractUsernameFromRefreshToken(token: String): String? {
        return extractClaim(token, getRefreshSignInKey(), Claims::getSubject)
    }

    fun extractExpiration(token: String): Date {
        return extractClaim(token, getAccessSignInKey(), Claims::getExpiration)
    }

    fun extractIssuedAtFromAccessToken(token: String): Date {
        return extractClaim(token, getAccessSignInKey(), Claims::getIssuedAt)
    }

    fun extractIssuedAtFromRefreshToken(token: String): Date {
        return extractClaim(token, getRefreshSignInKey(), Claims::getIssuedAt)
    }

    fun generateAccessToken(userDetails: UserDetails, tokenType: JwtTokenType = JwtTokenType.APPLICANT): String {
        return buildToken(mapOf(TOKEN_TYPE_CLAIM to tokenType.name), userDetails, jwtAccessExpirationTime, getAccessSignInKey())
    }

    fun getAccessTokenExpirationTime() = jwtAccessExpirationTime

    fun generateRefreshToken(userDetails: UserDetails, tokenType: JwtTokenType = JwtTokenType.APPLICANT): String {
        return buildToken(mapOf(TOKEN_TYPE_CLAIM to tokenType.name), userDetails, jwtRefreshExpirationTime, getRefreshSignInKey())
    }

    fun getRefreshTokenExpirationTime() = jwtRefreshExpirationTime

    fun isAccessTokenValid(
        token: String,
        userDetails: UserDetails,
        tokenType: JwtTokenType = JwtTokenType.APPLICANT
    ): Boolean {
        val username = extractUsernameFromAccessToken(token)
        return userDetails.username == username &&
            isTokenTypeValid(token, getAccessSignInKey(), tokenType) &&
            !isTokenExpired(token, getAccessSignInKey())
    }

    fun isRefreshTokenValid(
        token: String,
        userDetails: UserDetails,
        tokenType: JwtTokenType = JwtTokenType.APPLICANT
    ): Boolean {
        val username = extractUsernameFromRefreshToken(token)
        return userDetails.username == username &&
            isTokenTypeValid(token, getRefreshSignInKey(), tokenType) &&
            !isTokenExpired(token, getRefreshSignInKey())
    }

    private fun isTokenTypeValid(token: String, key: SecretKey, tokenType: JwtTokenType): Boolean {
        val actualTokenType = extractAllClaims(token, key)[TOKEN_TYPE_CLAIM] as String?
        return actualTokenType == tokenType.name
    }

    private fun buildToken(
        extraClaims: Map<String, Any>,
        userDetails: UserDetails,
        expirationTime: Long,
        key: SecretKey
    ): String {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(key)
            .compact()
    }

    private fun isTokenExpired(token: String, key: SecretKey): Boolean {
        return extractAllClaims(token, key).expiration.before(Date())
    }

    private fun <T> extractClaim(token: String, key: SecretKey, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token, key)
        return claimsResolver.invoke(claims)
    }

    private fun extractAllClaims(token: String, key: SecretKey): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getAccessSignInKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtAccessSecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    private fun getRefreshSignInKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtRefreshSecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}

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

    fun generateAccessToken(userDetails: UserDetails): String {
        return buildToken(mapOf(), userDetails, jwtAccessExpirationTime, getAccessSignInKey())
    }

    fun getAccessTokenExpirationTime() = jwtAccessExpirationTime

    fun generateRefreshToken(userDetails: UserDetails): String {
        return buildToken(mapOf(), userDetails, jwtRefreshExpirationTime, getRefreshSignInKey())
    }

    fun getRefreshTokenExpirationTime() = jwtRefreshExpirationTime

    fun isAccessTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsernameFromAccessToken(token)
        return userDetails.username == username && !isTokenExpired(token, getAccessSignInKey())
    }

    fun isRefreshTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsernameFromRefreshToken(token)
        return userDetails.username == username && !isTokenExpired(token, getRefreshSignInKey())
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

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
    @Value("\${security.jwt.secret}")
    private val jwtSecret: String,
    @Value("\${security.jwt.access.expiration-time}")
    private val jwtAccessExpirationTime: Long,
    @Value("\${security.jwt.refresh.expiration-time}")
    private val jwtRefreshExpirationTime: Long
) {
    fun extractUsername(token: String): String? {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    fun generateAccessToken(userDetails: UserDetails): String {
        return buildToken(mapOf(), userDetails, jwtAccessExpirationTime)
    }

    fun getAccessTokenExpirationTime() = jwtAccessExpirationTime

    fun generateRefreshToken(userDetails: UserDetails): String {
        return buildToken(mapOf(), userDetails, jwtRefreshExpirationTime)
    }

    fun getRefreshTokenExpirationTime() = jwtRefreshExpirationTime

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return userDetails.username.equals(username) && !isTokenExpired(token)
    }

    private fun buildToken(extraClaims: Map<String, Any>, userDetails: UserDetails, expirationTime: Long): String {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(getSignInKey())
            .compact()
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver.invoke(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getSignInKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtSecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
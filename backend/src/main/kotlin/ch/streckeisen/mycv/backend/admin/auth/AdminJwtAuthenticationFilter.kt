package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.JwtTokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Component
@Profile("!admin-bootstrap")
class AdminJwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val adminUserDetailsService: AdminUserDetailsService,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {
    override fun shouldNotFilterAsyncDispatch(): Boolean = false

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/api/admin")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = request.cookies?.find { cookie -> cookie.name == ADMIN_ACCESS_TOKEN_NAME }?.value

        if (accessToken.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val username = jwtService.extractUsernameFromAccessToken(accessToken)
            val authentication = SecurityContextHolder.getContext().authentication
            if (username != null && authentication == null) {
                authenticateAdmin(username, accessToken, request)
            }
            filterChain.doFilter(request, response)
        } catch (_: JwtException) {
            SecurityContextHolder.getContext().authentication = null
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.error("Failed to process admin authentication token", ex)
            handlerExceptionResolver.resolveException(request, response, null, ex)
        }
    }

    private fun authenticateAdmin(username: String, accessToken: String, request: HttpServletRequest) {
        adminUserDetailsService.loadAdminByUsernameAsResult(username)
            .onSuccess { userDetails ->
                if (jwtService.isAccessTokenValid(accessToken, userDetails, JwtTokenType.ADMIN)) {
                    val admin = userDetails.admin
                    if (!admin.isActive || isTemporaryPasswordExpired(admin) || isTokenIssuedBeforePasswordChange(
                            accessToken,
                            admin
                        )
                    ) {
                        SecurityContextHolder.getContext().authentication = null
                        return
                    }
                    val principal = AdminPrincipal(
                        admin.username,
                        admin.id!!,
                        admin.role,
                        admin.mustChangePassword
                    )
                    val authToken = UsernamePasswordAuthenticationToken(principal, null, userDetails.authorities)
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
            .onFailure {
                SecurityContextHolder.getContext().authentication = null
            }
    }

    private fun isTemporaryPasswordExpired(admin: ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity): Boolean {
        return admin.mustChangePassword && admin.temporaryPasswordExpiresAt?.isBefore(LocalDateTime.now()) == true
    }

    private fun isTokenIssuedBeforePasswordChange(
        accessToken: String,
        admin: ch.streckeisen.mycv.backend.admin.account.AdminAccountEntity
    ): Boolean {
        val passwordChangedAt = admin.passwordChangedAt ?: return false
        val issuedAt = jwtService.extractIssuedAtFromAccessToken(accessToken)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return issuedAt.isBefore(passwordChangedAt)
    }
}

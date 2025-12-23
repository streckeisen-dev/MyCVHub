package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.account.auth.ACCESS_TOKEN_NAME
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.util.Locale

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsServiceImpl,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = request.cookies?.find { cookie -> cookie.name == ACCESS_TOKEN_NAME }?.value

        if (accessToken.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val userEmail = jwtService.extractUsername(accessToken)

            val authentication = SecurityContextHolder.getContext().authentication
            if (userEmail != null && authentication == null) {
                authenticateUser(userEmail, accessToken, request)
            } else if (userEmail != null && authentication != null && authentication is UsernamePasswordAuthenticationToken) {
                val principal = authentication.principal as MyCvPrincipal
                if (principal.status != AccountStatus.VERIFIED) {
                    authenticateUser(userEmail, accessToken, request)
                }
            }
            filterChain.doFilter(request, response)
        } catch (_: JwtException) {
            SecurityContextHolder.getContext().authentication = null
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.error("Failed to process authentication token", ex)
            handlerExceptionResolver.resolveException(request, response, null, ex)
        }
    }

    private fun authenticateUser(userEmail: String, accessToken: String, request: HttpServletRequest) {
        userDetailsService.loadUserByUsernameAsResult(userEmail)
            .onSuccess { userDetails ->
                if (jwtService.isTokenValid(accessToken, userDetails)) {
                    val principal = MyCvPrincipal(
                        userDetails.username,
                        userDetails.account.id!!,
                        AccountStatus.ofAccount(userDetails.account),
                        userDetails.account.accountDetails?.language?.let { Locale.of(it) }
                    )
                    val authToken =
                        UsernamePasswordAuthenticationToken(principal, null, userDetails.authorities)
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
            .onFailure {
                SecurityContextHolder.getContext().authentication = null
            }
    }
}
package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.account.auth.ACCESS_TOKEN_NAME
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

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
            val jwt = accessToken
            val userEmail = jwtService.extractUsername(jwt)

            val authentication = SecurityContextHolder.getContext().authentication
            if (userEmail != null && authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    val principal = MyCvPrincipal(userDetails.username!!, userDetails.account.id!!)
                    val authToken =
                        UsernamePasswordAuthenticationToken(principal, null, userDetails.authorities)
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, ex)
        }
    }
}
package ch.streckeisen.mycv.backend.admin.auth

import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import ch.streckeisen.mycv.backend.security.annotations.RequiresAdminRole
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.rememberme.InvalidCookieException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/auth")
@RequiresAdminRole
class AdminAuthenticationResource(
    private val adminAuthenticationService: AdminAuthenticationService,
    private val adminAuthTokenService: AdminAuthTokenService
) {
    @PublicApi
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: AdminLoginRequestDto): ResponseEntity<Unit> {
        return adminAuthTokenService.handleAuthTokenResult(adminAuthenticationService.authenticate(loginRequest))
    }

    @PostMapping("/change-password")
    @RequiresAdminRole(allowMustChangePassword = true)
    fun changePassword(@RequestBody changePasswordDto: AdminChangePasswordDto): ResponseEntity<Unit> {
        val principal = SecurityContextHolder.getContext().getAdminPrincipal()
        return adminAuthTokenService.handleAuthTokenResult(
            adminAuthenticationService.changePassword(principal.id, changePasswordDto)
        )
    }

    @GetMapping("/login/verify")
    @RequiresAdminRole(allowMustChangePassword = true)
    fun verifyLogin(): ResponseEntity<AdminAuthResponseDto> {
        val principal = SecurityContextHolder.getContext().getAdminPrincipal()
        return ResponseEntity.ok(
            AdminAuthResponseDto(
                username = principal.username,
                role = principal.role,
                mustChangePassword = principal.mustChangePassword
            )
        )
    }

    @PublicApi
    @PostMapping("/refresh")
    fun refreshAccessToken(request: HttpServletRequest): ResponseEntity<Unit> {
        val refreshToken = request.cookies?.find { cookie -> cookie.name == ADMIN_REFRESH_TOKEN_NAME }?.value
            ?: throw InvalidCookieException("Admin refresh token required")

        val refreshResult = adminAuthenticationService.refreshAccessToken(refreshToken)
        return adminAuthTokenService.handleAuthTokenResult(refreshResult)
    }

    @PublicApi
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, adminAuthTokenService.createRefreshCookie("", 0).toString())
        headers.add(HttpHeaders.SET_COOKIE, adminAuthTokenService.createAccessCookie("", 0).toString())
        headers.add(
            HttpHeaders.SET_COOKIE, ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build()
                .toString()
        )
        return ResponseEntity.ok().headers(headers).build()
    }
}

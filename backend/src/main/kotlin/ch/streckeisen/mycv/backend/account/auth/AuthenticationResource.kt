package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.AccountStatus
import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.account.dto.AuthResponseDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.cv.profile.ThumbnailDto
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import ch.streckeisen.mycv.backend.security.annotations.RequiresAccountStatus
import ch.streckeisen.mycv.backend.security.getMyCvPrincipal
import ch.streckeisen.mycv.backend.security.getMyCvPrincipalOrNull
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
@RequestMapping(("/api/auth"))
class AuthenticationResource(
    private val authenticationService: AuthenticationService,
    private val authTokenService: AuthTokenService,
    private val applicantAccountService: ApplicantAccountService,
    private val profilePictureService: ProfilePictureService
) {
    @PublicApi
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequestDto): ResponseEntity<Unit> {
        val loginResult = authenticationService.authenticate(loginRequest)
        return authTokenService.handleAuthTokenResult(loginResult)
    }

    @RequiresAccountStatus(AccountStatus.INCOMPLETE)
    @GetMapping("/login/verify")
    fun verifyLogin(): ResponseEntity<AuthResponseDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipalOrNull()
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return applicantAccountService.findById(principal.id)
            .fold(
                onSuccess = { account ->
                    val displayName = if (account.accountDetails != null) {
                        account.accountDetails.firstName + " " + account.accountDetails.lastName
                    } else null
                    val hasProfile = account.profile != null
                    val thumbnail = if (hasProfile) {
                        profilePictureService.getThumbnail(account.id, account.profile).getOrNull()
                    } else null
                    ResponseEntity.ok(
                        AuthResponseDto(
                            username = principal.username,
                            authLevel = principal.status,
                            displayName = displayName,
                            language = account.accountDetails?.language,
                            hasProfile = hasProfile,
                            thumbnailDto = if (thumbnail != null) {
                                ThumbnailDto(thumbnail.uri.toString())
                            } else null
                        )
                    )
                },
                onFailure = {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            )

    }

    @PublicApi
    @PostMapping("/signup")
    fun signup(@RequestBody userRegistration: SignupRequestDto): ResponseEntity<Unit> {
        val loginResult = authenticationService.signUp(userRegistration)
        return authTokenService.handleAuthTokenResult(loginResult)
    }

    @PublicApi
    @PostMapping("/refresh")
    fun refreshAccessToken(request: HttpServletRequest): ResponseEntity<Unit> {
        val refreshToken = request.cookies?.find { cookie -> cookie.name == REFRESH_TOKEN_NAME }?.value
        if (refreshToken == null) {
            throw InvalidCookieException("${MYCV_KEY_PREFIX}.auth.tokenRefresh.refreshTokenRequired")
        }

        val refreshResult = authenticationService.refreshAccessToken(refreshToken)
        return authTokenService.handleAuthTokenResult(refreshResult)
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody changePasswordDto: ChangePasswordDto): ResponseEntity<AuthResponseDto> {
        val principal = SecurityContextHolder.getContext().authentication.getMyCvPrincipal()

        return authenticationService.changePassword(principal.id, changePasswordDto)
            .fold(
                onSuccess = {
                    ResponseEntity.ok().build()
                },
                onFailure = {
                    throw it
                }
            )
    }

    @PublicApi
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, authTokenService.createRefreshCookie("", 0).toString())
        headers.add(HttpHeaders.SET_COOKIE, authTokenService.createAccessCookie("", 0).toString())
        headers.add(
            HttpHeaders.SET_COOKIE, ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build()
                .toString()
        )
        return ResponseEntity.ok()
            .headers(headers)
            .build()
    }
}
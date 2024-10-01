package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import ch.streckeisen.mycv.backend.cv.applicant.ApplicantManager
import ch.streckeisen.mycv.backend.cv.applicant.ApplicantValidationService
import ch.streckeisen.mycv.backend.cv.applicant.PASSWORD_MAX_LENGTH
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.security.JwtService
import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val applicantManager: ApplicantManager,
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val applicantValidationService: ApplicantValidationService,
) {
    fun signUp(signupRequest: SignupRequestDto): Result<AuthData> {
        return applicantValidationService.validateSignupRequest(signupRequest)
            .fold(
                onSuccess = {
                    val encodedPassword = passwordEncoder.encode(signupRequest.password)
                    if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
                        val errors = ValidationException.ValidationErrorBuilder()
                        errors.addError("password", "Encoded password exceeds number of allowed characters")
                        Result.failure(errors.build("Password encoding failed"))
                    } else {
                        val applicant = Applicant(
                            signupRequest.firstName!!,
                            signupRequest.lastName!!,
                            signupRequest.email!!,
                            signupRequest.phone!!,
                            signupRequest.birthday!!,
                            signupRequest.street!!,
                            signupRequest.houseNumber!!,
                            signupRequest.postcode!!,
                            signupRequest.city!!,
                            signupRequest.country!!,
                            encodedPassword
                        )
                        applicantManager.create(applicant)
                        authenticate(LoginRequestDto(signupRequest.email, signupRequest.password))
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun authenticate(loginRequest: LoginRequestDto): Result<AuthData> {
        return validateLoginRequest(loginRequest)
            .fold(
                onSuccess = {
                    try {
                        authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken(
                                loginRequest.username,
                                loginRequest.password
                            )
                        )

                        val userDetails = userDetailsService.loadUserByUsername(loginRequest.username)
                        val accessToken = jwtService.generateAccessToken(userDetails)
                        val refreshToken = jwtService.generateRefreshToken(userDetails)
                        val accessTokenExpirationTime = jwtService.getAccessTokenExpirationTime()
                        val refreshTokenExpirationTime = jwtService.getRefreshTokenExpirationTime()
                        Result.success(
                            AuthData(
                                accessToken,
                                accessTokenExpirationTime,
                                refreshToken,
                                refreshTokenExpirationTime
                            )
                        )
                    } catch (ex: AuthenticationException) {
                        Result.failure(ex)
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun refreshAccessToken(refreshToken: String): Result<AuthData> {
        val username = jwtService.extractUsername(refreshToken)
        val userDetails = userDetailsService.loadUserByUsername(username)
        if (userDetails == null || !jwtService.isTokenValid(refreshToken, userDetails)) {
            return Result.failure(JwtException("Invalid refresh token"))
        }

        val accessToken = jwtService.generateAccessToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        return Result.success(
            AuthData(
                accessToken,
                jwtService.getAccessTokenExpirationTime(),
                refreshToken,
                jwtService.getRefreshTokenExpirationTime()
            )
        )
    }

    private fun validateLoginRequest(loginRequest: LoginRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (loginRequest.username.isNullOrBlank()) {
            validationErrorBuilder.addError("username", "Username cannot be blank")
        }

        if (loginRequest.password.isNullOrBlank()) {
            validationErrorBuilder.addError("password", "Password cannot be blank")
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Authentication failed with errors"))
        }
        return Result.success(Unit)
    }
}
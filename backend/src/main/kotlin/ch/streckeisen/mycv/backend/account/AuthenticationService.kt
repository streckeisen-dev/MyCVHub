package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import ch.streckeisen.mycv.backend.cv.applicant.ApplicantService
import ch.streckeisen.mycv.backend.cv.applicant.ApplicantValidationService
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.security.JwtService
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AuthenticationService(
    private val applicantService: ApplicantService,
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val applicantValidationService: ApplicantValidationService,
) {
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun registerNewUser(signupRequest: UserRegistrationDto): Result<AuthData> {
        return validateSignupRequest(signupRequest)
            .fold(
                onSuccess = {
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
                        passwordEncoder.encode(signupRequest.password)
                    )
                    applicantService.create(applicant)
                    authenticate(LoginRequestDto(signupRequest.email, signupRequest.password))
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
            throw JwtException("Invalid refresh token")
        }

        val accessToken = jwtService.generateAccessToken(userDetails)
        val refreshToken = jwtService.generateRefreshToken(userDetails)
        return Result.success(AuthData(accessToken, jwtService.getAccessTokenExpirationTime(), refreshToken, jwtService.getRefreshTokenExpirationTime()))
    }

    private fun validateSignupRequest(signupRequest: UserRegistrationDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (signupRequest.firstName.isNullOrBlank()) {
            validationErrorBuilder.addError("firstName", "First name must not be blank")
        }

        if (signupRequest.lastName.isNullOrBlank()) {
            validationErrorBuilder.addError("lastName", "Last name must not be blank")
        }

        applicantValidationService.validateEmail(validationErrorBuilder, null, signupRequest.email)

        if (signupRequest.street.isNullOrBlank()) {
            validationErrorBuilder.addError("street", "Street must not be blank")
        }

        if (signupRequest.postcode.isNullOrBlank()) {
            validationErrorBuilder.addError("postcode", "Postcode must not be blank")
        }

        if (signupRequest.city.isNullOrBlank()) {
            validationErrorBuilder.addError("city", "City must not be blank")
        }

        if (signupRequest.country.isNullOrBlank()) {
            validationErrorBuilder.addError("country", "Country must not be blank")
        } else if (signupRequest.country.length != 2) {
            validationErrorBuilder.addError("country", "Country must be ISO 2 character country code")
        } else if (!phoneNumberUtil.supportedRegions.contains(signupRequest.country)) {
            validationErrorBuilder.addError("country", "Invalid country code")
        }


        if (signupRequest.phone.isNullOrBlank()) {
            validationErrorBuilder.addError("phone", "Phone must not be blank")
        } else {
            try {
                val phoneNumber = phoneNumberUtil.parse(signupRequest.phone, signupRequest.country)
                val isValidNumber = phoneNumberUtil.isValidNumber(phoneNumber)
                if (!isValidNumber) {
                    validationErrorBuilder.addError("phone", "Phone number is not valid")
                }
            } catch (ex: NumberParseException) {
                validationErrorBuilder.addError("phone", "Couldn't parse phone number")
            }
        }

        if (signupRequest.birthday == null) {
            validationErrorBuilder.addError("birthday", "Birthday must not be blank")
        } else if (signupRequest.birthday.isAfter(LocalDate.now())) {
            validationErrorBuilder.addError("birthday", "Birthday must be in the past")
        }

        if (signupRequest.password.isNullOrBlank()) {
            validationErrorBuilder.addError("password", "Password must not be blank")
        } else {
            applicantValidationService.validatePassword(validationErrorBuilder, signupRequest.password)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Signup failed with errors"))
        }
        return Result.success(Unit)
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
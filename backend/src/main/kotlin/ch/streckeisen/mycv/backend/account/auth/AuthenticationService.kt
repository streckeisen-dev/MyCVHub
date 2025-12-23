package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.PASSWORD_MAX_LENGTH
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.account.verification.AccountVerificationService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private const val ENCODED_PASSWORD_LENGTH_ERROR_KEY = "${MYCV_KEY_PREFIX}.account.validations.password.encodingTooLong"
private const val PASSWORD_ENCODING_ERROR_KEY = "${MYCV_KEY_PREFIX}.account.validations.password.encodingError"

private val logger = KotlinLogging.logger {}

@Service
class AuthenticationService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val authenticationValidationService: AuthenticationValidationService,
    private val authenticationManager: AuthenticationManager,
    private val authTokenService: AuthTokenService,
    private val messagesService: MessagesService,
    private val passwordEncoder: PasswordEncoder,
    private val accountVerificationService: AccountVerificationService
) {
    fun signUp(signupRequest: SignupRequestDto): Result<AuthTokens> {
        return createAccount(signupRequest)
            .fold(
                onSuccess = {
                    authenticate(LoginRequestDto(signupRequest.username, signupRequest.password))
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun authenticate(loginRequest: LoginRequestDto): Result<AuthTokens> {
        return authenticationValidationService.validateLoginRequest(loginRequest)
            .fold(
                onSuccess = {
                    try {
                        authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken(
                                loginRequest.username,
                                loginRequest.password
                            )
                        )
                        authTokenService.generateAuthData(loginRequest.username!!)
                    } catch (ex: AuthenticationException) {
                        Result.failure(ex)
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun refreshAccessToken(oldRefreshToken: String): Result<AuthTokens> {
        try {
            val username = authTokenService.validateRefreshToken(oldRefreshToken)
                .getOrElse { return Result.failure(it) }
            return authTokenService.generateAuthData(username)
        } catch (ex: BadCredentialsException) {
            return Result.failure(ex)
        } catch (ex: JwtException) {
            return Result.failure(ex)
        }
    }

    @Transactional
    fun changePassword(accountId: Long, changePasswordDto: ChangePasswordDto): Result<ApplicantAccountEntity> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }

        if (existingAccount.password == null) {
            throw LocalizedException("${MYCV_KEY_PREFIX}.auth.changePassword.passwordNotSet")
        }

        authenticationValidationService.validateChangePasswordRequest(changePasswordDto, existingAccount.password)
            .onFailure { return Result.failure(it) }

        val encodedNewPassword = encodePassword(changePasswordDto.password)
            .getOrElse { return Result.failure(it) }

        val account = applicantAccountRepository.setPassword(accountId, encodedNewPassword)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound")) }
        return Result.success(account)
    }

    @Transactional(readOnly = false)
    private fun createAccount(signupRequest: SignupRequestDto): Result<ApplicantAccountEntity> {
        authenticationValidationService.validateSignupRequest(signupRequest)
            .onFailure { return Result.failure(it) }
        val encodedPassword = encodePassword(signupRequest.password)
            .getOrElse { return Result.failure(it) }

        val applicantAccount = ApplicantAccountEntity(
            username = signupRequest.username!!,
            password = encodedPassword,
            isOAuthUser = false,
            isVerified = false,
            accountDetails = AccountDetailsEntity(
                signupRequest.firstName!!,
                signupRequest.lastName!!,
                signupRequest.email!!,
                signupRequest.phone!!,
                signupRequest.birthday!!,
                signupRequest.street!!,
                signupRequest.houseNumber,
                signupRequest.postcode!!,
                signupRequest.city!!,
                signupRequest.country!!,
                signupRequest.language!!
            )
        )
        val account = applicantAccountRepository.save(applicantAccount)
        accountVerificationService.generateVerificationToken(account.id!!)
            .onFailure { logger.error(it) { "[Account ${account.id}] Failed to generate verification token" } }
        return Result.success(account)
    }

    private fun encodePassword(password: String?): Result<String> {
        val encodedPassword = passwordEncoder.encode(password)
        if (encodedPassword.length > PASSWORD_MAX_LENGTH) {
            val errors = ValidationException.ValidationErrorBuilder()
            errors.addError("password", messagesService.getMessage(ENCODED_PASSWORD_LENGTH_ERROR_KEY))
            return Result.failure(errors.build(messagesService.getMessage(PASSWORD_ENCODING_ERROR_KEY)))
        }
        return Result.success(encodedPassword)
    }
}
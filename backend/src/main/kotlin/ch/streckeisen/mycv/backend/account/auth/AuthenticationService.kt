package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountRepository
import ch.streckeisen.mycv.backend.account.PASSWORD_MAX_LENGTH
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.account.oauth.OAuthIntegrationService
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import ch.streckeisen.mycv.backend.security.JwtService
import ch.streckeisen.mycv.backend.security.UserDetailsServiceImpl
import io.jsonwebtoken.JwtException
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private const val ENCODED_PASSWORD_LENGTH_ERROR_KEY = "${MYCV_KEY_PREFIX}.account.validation.password.encodingTooLong"
private const val PASSWORD_ENCODING_ERROR_KEY = "${MYCV_KEY_PREFIX}.account.validation.password.encodingError"

private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

@Service
class AuthenticationService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val oauthIntegrationService: OAuthIntegrationService,
    private val authenticationValidationService: AuthenticationValidationService,
    private val userDetailsService: UserDetailsServiceImpl,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val messagesService: MessagesService,
    private val passwordEncoder: PasswordEncoder,
) {
    fun signUp(signupRequest: SignupRequestDto): Result<AuthData> {
        return create(signupRequest)
            .fold(
                onSuccess = {
                    authenticate(LoginRequestDto(signupRequest.email, signupRequest.password))
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun authenticate(loginRequest: LoginRequestDto): Result<AuthData> {
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
                        generateAuthData(loginRequest.username!!)
                    } catch (ex: AuthenticationException) {
                        Result.failure(ex)
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
    }

    fun authenticateGitHubUser(githubId: String, username: String): Result<AuthData> {
        return oauthIntegrationService.findByGithubId(githubId).fold(
            onSuccess = { oauthIntegration ->
                generateAuthData(oauthIntegration.account.username)
            },
            onFailure = {
                try {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    logger.info("[Account {}] Adding GitHub integration for account", userDetails.account.id)
                    oauthIntegrationService.addGithubIntegration(userDetails.account, githubId)
                    generateAuthData(userDetails.account.username)
                } catch (ex: BadCredentialsException) {
                    logger.info("Successful OAuth authentication, but couldn't find matching account. Create incomplete account...")
                    Result.failure(ex)
                }
            }
        )
    }

    fun refreshAccessToken(oldRefreshToken: String): Result<AuthData> {
        try {
            val username = jwtService.extractUsername(oldRefreshToken)
            val userDetails = userDetailsService.loadUserByUsername(username)
            if (!jwtService.isTokenValid(oldRefreshToken, userDetails)) {
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
        } catch (ex: BadCredentialsException) {
            return Result.failure(ex)
        } catch (ex: JwtException) {
            return Result.failure(ex)
        }
    }

    @Transactional
    fun changePassword(accountId: Long, changePasswordDto: ChangePasswordDto): Result<ApplicantAccountEntity> {
        val existingAccount = applicantAccountRepository.findById(accountId)
            .getOrElse { return Result.failure(EntityNotFoundException("Account does not exist")) }

        if (existingAccount.password == null) {
            throw AccessDeniedException("Change password requires a set password")
        }

        authenticationValidationService.validateChangePasswordRequest(changePasswordDto, existingAccount.password)
            .onFailure { return Result.failure(it) }

        val encodedNewPassword = encodePassword(changePasswordDto.password)
            .getOrElse { return Result.failure(it) }

        val account = ApplicantAccountEntity(
            existingAccount.username,
            encodedNewPassword,
            existingAccount.isOAuthUser,
            accountDetails = existingAccount.accountDetails,
            id = existingAccount.id,
            profile = existingAccount.profile
        )

        return Result.success(applicantAccountRepository.save(account))
    }

    private fun generateAuthData(username: String): Result<AuthData> {
        try {
            val userDetails = userDetailsService.loadUserByUsername(username)
            val accessToken = jwtService.generateAccessToken(userDetails)
            val refreshToken = jwtService.generateRefreshToken(userDetails)
            val accessTokenExpirationTime = jwtService.getAccessTokenExpirationTime()
            val refreshTokenExpirationTime = jwtService.getRefreshTokenExpirationTime()
            return Result.success(
                AuthData(
                    accessToken,
                    accessTokenExpirationTime,
                    refreshToken,
                    refreshTokenExpirationTime
                )
            )
        } catch (ex: AuthenticationException) {
            return Result.failure(ex)
        } catch (ex: JwtException) {
            return Result.failure(ex)
        }
    }

    @Transactional(readOnly = false)
    private fun create(signupRequest: SignupRequestDto): Result<ApplicantAccountEntity> {
        authenticationValidationService.validateSignupRequest(signupRequest)
            .onFailure { return Result.failure(it) }
        val encodedPassword = encodePassword(signupRequest.password)
            .getOrElse { return Result.failure(it) }

        val applicantAccount = ApplicantAccountEntity(
            signupRequest.email!!,
            encodedPassword,
            false,
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
                signupRequest.country!!
            )
        )
        return Result.success(applicantAccountRepository.save(applicantAccount))
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
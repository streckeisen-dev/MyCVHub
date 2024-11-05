package ch.streckeisen.mycv.backend.account.auth

import ch.streckeisen.mycv.backend.account.ApplicantAccountValidationService
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.LoginRequestDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

private const val MIN_PASSWORD_LENGTH = 8

private const val PASSWORD_FIELD_KEY = "password"
private const val CONFIRM_PASSWORD_FIELD_KEY = "confirmPassword"
private const val OLD_PASSWORD_FIELD_KEY = "oldPassword"

private const val PASSWORD_KEY_PREFIX = "${MYCV_KEY_PREFIX}.passwordRequirements"
private const val PASSWORD_LENGTH_KEY = "${PASSWORD_KEY_PREFIX}.length"
private const val PASSWORD_WHITESPACE_KEY = "${PASSWORD_KEY_PREFIX}.whitespaces"
private const val PASSWORD_DIGITS_KEY = "${PASSWORD_KEY_PREFIX}.digits"
private const val PASSWORD_UPPERCASE_KEY = "${PASSWORD_KEY_PREFIX}.uppercase"
private const val PASSWORD_LOWERCASE_KEY = "${PASSWORD_KEY_PREFIX}.lowercase"
private const val PASSWORD_SPECIAL_CHARS_KEY = "${PASSWORD_KEY_PREFIX}.specialChars"
private const val PASSWORD_MATCH_KEY = "${PASSWORD_KEY_PREFIX}.match"
private const val ACCOUNT_VALIDATION_KEY_PREFIX = "${MYCV_KEY_PREFIX}.account.validation"
private const val OLD_PASSWORD_INVALID_ERROR_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.oldPasswordInvalid"
private const val LOGIN_VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.auth.login.error"
private const val SIGNUP_VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.auth.signup.error"
private const val CHANGE_PASSWORD_VALIDATION_ERROR_KEY = "${MYCV_KEY_PREFIX}.auth.changePassword.error"

@Service
class AuthenticationValidationService(
    private val messagesService: MessagesService,
    private val applicantAccountValidationService: ApplicantAccountValidationService,
    private val passwordEncoder: PasswordEncoder
) {
    fun validateLoginRequest(loginRequest: LoginRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        if (loginRequest.username.isNullOrBlank()) {
            val error = messagesService.getMessage("username")
            validationErrorBuilder.addError("username", error)
        }

        if (loginRequest.password.isNullOrBlank()) {
            val error = messagesService.getMessage("password")
            validationErrorBuilder.addError("password", error)
        }

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(LOGIN_VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }

    fun validateSignupRequest(signupRequest: SignupRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        applicantAccountValidationService.validateFirstName(signupRequest.firstName, validationErrorBuilder)
        applicantAccountValidationService.validateLastName(signupRequest.lastName, validationErrorBuilder)
        applicantAccountValidationService.validateEmail(signupRequest.email, null, validationErrorBuilder)
        applicantAccountValidationService.validateStreet(signupRequest.street, validationErrorBuilder)
        applicantAccountValidationService.validateHouseNumber(signupRequest.houseNumber, validationErrorBuilder)
        applicantAccountValidationService.validatePostcode(signupRequest.postcode, validationErrorBuilder)
        applicantAccountValidationService.validateCity(signupRequest.city, validationErrorBuilder)
        applicantAccountValidationService.validateCountry(signupRequest.country, validationErrorBuilder)
        applicantAccountValidationService.validatePhone(
            signupRequest.phone,
            signupRequest.country,
            validationErrorBuilder
        )
        applicantAccountValidationService.validateBirthday(signupRequest.birthday, validationErrorBuilder)
        validatePassword(
            signupRequest.password,
            signupRequest.confirmPassword,
            validationErrorBuilder
        )

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(SIGNUP_VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }

    fun validateChangePasswordRequest(
        changePassword: ChangePasswordDto,
        currentPassword: String
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        validateOldPassword(changePassword.oldPassword, currentPassword, validationErrorBuilder)
        validatePassword(changePassword.password, changePassword.confirmPassword, validationErrorBuilder)

        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(CHANGE_PASSWORD_VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }

    fun validatePassword(
        password: String?,
        confirmPassword: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (password.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(PASSWORD_FIELD_KEY)
            validationErrorBuilder.addError(PASSWORD_FIELD_KEY, error)
        }
        if (confirmPassword.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(CONFIRM_PASSWORD_FIELD_KEY)
            validationErrorBuilder.addError(CONFIRM_PASSWORD_FIELD_KEY, error)
        }
        if (password != null && confirmPassword != null) {
            validatePassword(password, confirmPassword)
                .onFailure { validationErrorBuilder.addError(PASSWORD_FIELD_KEY, it.message!!) }
        }
    }

    private fun validatePassword(password: String, confirmPassword: String) = runCatching {
        require(password.length >= MIN_PASSWORD_LENGTH) {
            messagesService.getMessage(
                PASSWORD_LENGTH_KEY,
                MIN_PASSWORD_LENGTH.toString()
            )
        }
        require(password.none { it.isWhitespace() }) { messagesService.getMessage(PASSWORD_WHITESPACE_KEY) }
        require(password.any { it.isDigit() }) { messagesService.getMessage(PASSWORD_DIGITS_KEY) }
        require(password.any { it.isUpperCase() }) { messagesService.getMessage(PASSWORD_UPPERCASE_KEY) }
        require(password.any { it.isLowerCase() }) { messagesService.getMessage(PASSWORD_LOWERCASE_KEY) }
        require(password.any { !it.isLetterOrDigit() }) { messagesService.getMessage(PASSWORD_SPECIAL_CHARS_KEY) }
        require(password == confirmPassword) { messagesService.getMessage(PASSWORD_MATCH_KEY) }
    }

    private fun validateOldPassword(
        oldPassword: String?,
        currentPassword: String,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (oldPassword.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(OLD_PASSWORD_FIELD_KEY)
            validationErrorBuilder.addError(OLD_PASSWORD_FIELD_KEY, error)
        } else if (!passwordEncoder.matches(oldPassword, currentPassword)) {
            val error = messagesService.getMessage(OLD_PASSWORD_INVALID_ERROR_KEY)
            validationErrorBuilder.addError(OLD_PASSWORD_FIELD_KEY, error)
        }
    }
}
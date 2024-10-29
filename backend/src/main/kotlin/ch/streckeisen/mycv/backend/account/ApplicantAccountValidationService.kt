package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.account.dto.ChangePasswordDto
import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Locale

private const val MIN_PASSWORD_LENGTH = 8

private const val ACCOUNT_VALIDATION_KEY_PREFIX = "${MYCV_KEY_PREFIX}.account.validation"
private const val EMAIL_INVALID_KEY = "${MYCV_KEY_PREFIX}.validations.email"
private const val EMAIL_TAKEN_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.emailAlreadyTaken"
private const val PHONE_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.phoneInvalid"
private const val EMPTY_HOUSE_NUMBER_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.houseNumberEmpty"
private const val COUNTRY_LENGTH_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.countryLengthError"
private const val COUNTRY_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.countryInvalid"
private const val PASSWORD_KEY_PREFIX = "${MYCV_KEY_PREFIX}.passwordRequirements"
private const val PASSWORD_LENGTH_KEY = "${PASSWORD_KEY_PREFIX}.length"
private const val PASSWORD_WHITESPACE_KEY = "${PASSWORD_KEY_PREFIX}.whitespaces"
private const val PASSWORD_DIGITS_KEY = "${PASSWORD_KEY_PREFIX}.digits"
private const val PASSWORD_UPPERCASE_KEY = "${PASSWORD_KEY_PREFIX}.uppercase"
private const val PASSWORD_LOWERCASE_KEY = "${PASSWORD_KEY_PREFIX}.lowercase"
private const val PASSWORD_SPECIAL_CHARS_KEY = "${PASSWORD_KEY_PREFIX}.specialChars"
private const val PASSWORD_MATCH_KEY = "${PASSWORD_KEY_PREFIX}.match"
private const val OLD_PASSWORD_INVALID_ERROR_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.oldPasswordInvalid"
private const val VALIDATION_ERROR_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.error"

private const val FIRST_NAME_FIELD_KEY = "firstName"
private const val LAST_NAME_FIELD_KEY = "lastName"
private const val EMAIL_FIELD_KEY = "email"
private const val BIRTHDAY_FIELD_KEY = "birthday"
private const val PHONE_FIELD_KEY = "phone"
private const val STREET_FIELD_KEY = "street"
private const val HOUSE_NUMBER_FIELD_KEY = "houseNumber"
private const val POSTCODE_FIELD_KEY = "postcode"
private const val CITY_FIELD_KEY = "city"
private const val COUNTRY_FIELD_KEY = "country"
private const val PASSWORD_FIELD_KEY = "password"
private const val CONFIRM_PASSWORD_FIELD_KEY = "confirmPassword"
private const val OLD_PASSWORD_FIELD_KEY = "oldPassword"

@Service
class ApplicantAccountValidationService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val messagesService: MessagesService,
    private val passwordEncoder: PasswordEncoder
) {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    fun validateSignupRequest(signupRequest: SignupRequestDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        validateFirstName(signupRequest.firstName, validationErrorBuilder)
        validateLastName(signupRequest.lastName, validationErrorBuilder)
        validateEmail(signupRequest.email, null, validationErrorBuilder)
        validateStreet(signupRequest.street, validationErrorBuilder)
        validateHouseNumber(signupRequest.houseNumber, validationErrorBuilder)
        validatePostcode(signupRequest.postcode, validationErrorBuilder)
        validateCity(signupRequest.city, validationErrorBuilder)
        validateCountry(signupRequest.country, validationErrorBuilder)
        validatePhone(signupRequest.phone, signupRequest.country, validationErrorBuilder)
        validateBirthday(signupRequest.birthday, validationErrorBuilder)
        validatePassword(signupRequest.password, signupRequest.confirmPassword, validationErrorBuilder)

        return checkValidationResult(validationErrorBuilder)
    }

    fun validateAccountUpdate(accountId: Long, accountUpdate: AccountUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        validateFirstName(accountUpdate.firstName, validationErrorBuilder)
        validateLastName(accountUpdate.lastName, validationErrorBuilder)
        validateEmail(accountUpdate.email, accountId, validationErrorBuilder)
        validateStreet(accountUpdate.street, validationErrorBuilder)
        validateHouseNumber(accountUpdate.houseNumber, validationErrorBuilder)
        validatePostcode(accountUpdate.postcode, validationErrorBuilder)
        validateCity(accountUpdate.city, validationErrorBuilder)
        validateCountry(accountUpdate.country, validationErrorBuilder)
        validatePhone(accountUpdate.phone, accountUpdate.country, validationErrorBuilder)
        validateBirthday(accountUpdate.birthday, validationErrorBuilder)

        return checkValidationResult(validationErrorBuilder)
    }

    fun validateChangePasswordRequest(
        changePassword: ChangePasswordDto,
        currentPassword: String
    ): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        validateOldPassword(changePassword.oldPassword, currentPassword, validationErrorBuilder)
        validatePassword(changePassword.password, changePassword.confirmPassword, validationErrorBuilder)

        return checkValidationResult(validationErrorBuilder)
    }

    private fun validateFirstName(
        firstName: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (firstName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(FIRST_NAME_FIELD_KEY)
            validationErrorBuilder.addError(FIRST_NAME_FIELD_KEY, error)
        } else if (firstName.length > FIRST_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(FIRST_NAME_FIELD_KEY, FIRST_NAME_MAX_LENGTH)
            validationErrorBuilder.addError(FIRST_NAME_FIELD_KEY, error)
        }
    }

    private fun validateLastName(
        lastName: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (lastName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(LAST_NAME_FIELD_KEY)
            validationErrorBuilder.addError(LAST_NAME_FIELD_KEY, error)
        } else if (lastName.length > LAST_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(LAST_NAME_FIELD_KEY, LAST_NAME_MAX_LENGTH)
            validationErrorBuilder.addError(LAST_NAME_FIELD_KEY, error)
        }
    }

    private fun validateEmail(
        email: String?,
        updateId: Long?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder,
    ) {
        if (email.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(EMAIL_FIELD_KEY)
            validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
        } else if (!EmailValidator.getInstance().isValid(email)) {
            val error = messagesService.getMessage(EMAIL_INVALID_KEY)
            validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
        } else if (email.length > EMAIL_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(EMAIL_FIELD_KEY, EMAIL_MAX_LENGTH)
            validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
        } else {
            val applicant = applicantAccountRepository.findByEmail(email)
            if (applicant.isPresent && applicant.get().id != updateId) {
                val error = messagesService.getMessage(EMAIL_TAKEN_KEY)
                validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
            }
        }
    }

    private fun validateBirthday(
        birthday: LocalDate?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (birthday == null) {
            val error = messagesService.requiredFieldMissingError(BIRTHDAY_FIELD_KEY)
            validationErrorBuilder.addError(BIRTHDAY_FIELD_KEY, error)
        } else if (birthday.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError(BIRTHDAY_FIELD_KEY)
            validationErrorBuilder.addError(BIRTHDAY_FIELD_KEY, error)
        }
    }

    private fun validatePhone(
        phone: String?,
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (phone.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(PHONE_FIELD_KEY)
            validationErrorBuilder.addError(PHONE_FIELD_KEY, error)
        } else if (phone.length > PHONE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(PHONE_FIELD_KEY, PHONE_MAX_LENGTH)
            validationErrorBuilder.addError(PHONE_FIELD_KEY, error)
        } else {
            try {
                val phoneNumber = phoneNumberUtil.parse(phone, country ?: Locale.getDefault().country)
                val isValidNumber = phoneNumberUtil.isValidNumber(phoneNumber)
                if (!isValidNumber) {
                    val error = messagesService.getMessage(PHONE_INVALID_KEY)
                    validationErrorBuilder.addError(PHONE_FIELD_KEY, error)
                }
            } catch (ex: NumberParseException) {
                val error = messagesService.getMessage(PHONE_INVALID_KEY)
                validationErrorBuilder.addError(PHONE_FIELD_KEY, error)
            }
        }
    }

    private fun validateStreet(
        street: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (street.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(STREET_FIELD_KEY)
            validationErrorBuilder.addError(STREET_FIELD_KEY, error)
        } else if (street.length > STREET_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(STREET_FIELD_KEY, STREET_MAX_LENGTH)
            validationErrorBuilder.addError(STREET_FIELD_KEY, error)
        }
    }

    private fun validateHouseNumber(
        houseNumber: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (houseNumber == "") {
            val error = messagesService.getMessage(EMPTY_HOUSE_NUMBER_KEY)
            validationErrorBuilder.addError(HOUSE_NUMBER_FIELD_KEY, error)
        } else if (houseNumber != null && houseNumber.length > HOUSE_NUMBER_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(HOUSE_NUMBER_FIELD_KEY, HOUSE_NUMBER_MAX_LENGTH)
            validationErrorBuilder.addError(HOUSE_NUMBER_FIELD_KEY, error)
        }
    }

    private fun validatePostcode(
        postcode: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (postcode.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(POSTCODE_FIELD_KEY)
            validationErrorBuilder.addError(POSTCODE_FIELD_KEY, error)
        } else if (postcode.length > POSTCODE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(POSTCODE_FIELD_KEY, POSTCODE_MAX_LENGTH)
            validationErrorBuilder.addError(POSTCODE_FIELD_KEY, error)
        }
    }

    private fun validateCity(
        city: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (city.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(CITY_FIELD_KEY)
            validationErrorBuilder.addError(CITY_FIELD_KEY, error)
        } else if (city.length > CITY_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(CITY_FIELD_KEY, CITY_MAX_LENGTH)
            validationErrorBuilder.addError(CITY_FIELD_KEY, error)
        }
    }

    private fun validateCountry(
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (country.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(COUNTRY_FIELD_KEY)
            validationErrorBuilder.addError(COUNTRY_FIELD_KEY, error)
        } else if (country.length != COUNTRY_MAX_LENGTH) {
            val error = messagesService.getMessage(COUNTRY_LENGTH_KEY, COUNTRY_MAX_LENGTH.toString())
            validationErrorBuilder.addError(COUNTRY_FIELD_KEY, error)
        } else if (!phoneNumberUtil.supportedRegions.contains(country)) {
            val error = messagesService.getMessage(COUNTRY_INVALID_KEY)
            validationErrorBuilder.addError(COUNTRY_FIELD_KEY, error)
        }
    }

    private fun validatePassword(
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

    private fun checkValidationResult(validationErrorBuilder: ValidationException.ValidationErrorBuilder): Result<Unit> {
        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
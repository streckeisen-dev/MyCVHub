package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Locale

private const val MIN_PASSWORD_LENGTH = 8

private const val ACCOUNT_VALIDATION_KEY_PREFIX = "${MYCV_KEY_PREFIX}.account.validation"
private const val EMAIL_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.emailInvalid"
private const val EMAIL_TAKEN_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.emailAlreadyTaken"
private const val PHONE_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.phoneInvalid"
private const val EMPTY_HOUSE_NUMBER_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.houseNumberEmpty"
private const val COUNTRY_LENGTH_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.countryLengthError"
private const val COUNTRY_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.countryInvalid"
private const val PASSWORD_KEY_PREFIX = "${ACCOUNT_VALIDATION_KEY_PREFIX}.password"
private const val PASSWORD_LENGTH_KEY = "${PASSWORD_KEY_PREFIX}.length"
private const val PASSWORD_WHITESPACE_KEY = "${PASSWORD_KEY_PREFIX}.whitespaces"
private const val PASSWORD_DIGITS_KEY = "${PASSWORD_KEY_PREFIX}.digits"
private const val PASSWORD_UPPERCASE_KEY = "${PASSWORD_KEY_PREFIX}.uppercase"
private const val PASSWORD_LOWERCASE_KEY = "${PASSWORD_KEY_PREFIX}.lowercase"
private const val PASSWORD_SPECIAL_CHARS_KEY = "${PASSWORD_KEY_PREFIX}.specialChars"
private const val VALIDATION_ERROR_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.error"

@Service
class ApplicantAccountValidationService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val messagesService: MessagesService
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
        validatePassword(signupRequest.password, validationErrorBuilder)

        return checkValidationResult(validationErrorBuilder)
    }

    private fun validateFirstName(
        firstName: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (firstName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("firstName")
            validationErrorBuilder.addError("firstName", error)
        } else if (firstName.length > FIRST_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("firstName", FIRST_NAME_MAX_LENGTH)
            validationErrorBuilder.addError("firstName", error)
        }
    }

    private fun validateLastName(
        lastName: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (lastName.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("lastName")
            validationErrorBuilder.addError("lastName", error)
        } else if (lastName.length > LAST_NAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("lastName", LAST_NAME_MAX_LENGTH)
            validationErrorBuilder.addError("lastName", error)
        }
    }

    private fun validateEmail(
        email: String?,
        updateId: Long?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder,
    ) {
        if (email.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("email")
            validationErrorBuilder.addError("email", error)
        } else if (!EmailValidator.getInstance().isValid(email)) {
            val error = messagesService.getMessage(EMAIL_INVALID_KEY)
            validationErrorBuilder.addError("email", error)
        } else if (email.length > EMAIL_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("email", EMAIL_MAX_LENGTH)
            validationErrorBuilder.addError("email", error)
        } else {
            val applicant = applicantAccountRepository.findByEmail(email)
            if (applicant.isPresent && applicant.get().id != updateId) {
                val error = messagesService.getMessage(EMAIL_TAKEN_KEY)
                validationErrorBuilder.addError("email", error)
            }
        }
    }

    private fun validateBirthday(
        birthday: LocalDate?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (birthday == null) {
            val error = messagesService.requiredFieldMissingError("birthday")
            validationErrorBuilder.addError("birthday", error)
        } else if (birthday.isAfter(LocalDate.now())) {
            val error = messagesService.dateIsInFutureError("birthday")
            validationErrorBuilder.addError("birthday", error)
        }
    }

    private fun validatePhone(
        phone: String?,
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (phone.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("phone")
            validationErrorBuilder.addError("phone", error)
        } else if (phone.length > PHONE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("phone", PHONE_MAX_LENGTH)
            validationErrorBuilder.addError("phone", error)
        } else {
            try {
                val phoneNumber = phoneNumberUtil.parse(phone, country ?: Locale.getDefault().country)
                val isValidNumber = phoneNumberUtil.isValidNumber(phoneNumber)
                if (!isValidNumber) {
                    val error = messagesService.getMessage(PHONE_INVALID_KEY)
                    validationErrorBuilder.addError("phone", error)
                }
            } catch (ex: NumberParseException) {
                val error = messagesService.getMessage(PHONE_INVALID_KEY)
                validationErrorBuilder.addError("phone", error)
            }
        }
    }

    private fun validateStreet(
        street: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (street.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("street")
            validationErrorBuilder.addError("street", error)
        } else if (street.length > STREET_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("street", STREET_MAX_LENGTH)
            validationErrorBuilder.addError("street", error)
        }
    }

    private fun validateHouseNumber(
        houseNumber: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (houseNumber == "") {
            val error = messagesService.getMessage(EMPTY_HOUSE_NUMBER_KEY)
            validationErrorBuilder.addError("houseNumber", error)
        } else if (houseNumber != null && houseNumber.length > HOUSE_NUMBER_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("houseNumber", HOUSE_NUMBER_MAX_LENGTH)
            validationErrorBuilder.addError("houseNumber", error)
        }
    }

    private fun validatePostcode(
        postcode: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (postcode.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("postcode")
            validationErrorBuilder.addError("postcode", error)
        } else if (postcode.length > POSTCODE_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("postcode", POSTCODE_MAX_LENGTH)
            validationErrorBuilder.addError("postcode", error)
        }
    }

    private fun validateCity(
        city: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (city.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("city")
            validationErrorBuilder.addError("city", error)
        } else if (city.length > CITY_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError("city", CITY_MAX_LENGTH)
            validationErrorBuilder.addError("city", error)
        }
    }

    private fun validateCountry(
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (country.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("country")
            validationErrorBuilder.addError("country", error)
        } else if (country.length != COUNTRY_MAX_LENGTH) {
            val error = messagesService.getMessage(COUNTRY_LENGTH_KEY, COUNTRY_MAX_LENGTH.toString())
            validationErrorBuilder.addError("country", error)
        } else if (!phoneNumberUtil.supportedRegions.contains(country)) {
            val error = messagesService.getMessage(COUNTRY_INVALID_KEY)
            validationErrorBuilder.addError("country", error)
        }
    }

    private fun validatePassword(
        password: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (password.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError("password")
            validationErrorBuilder.addError("password", error)
        } else {
            validatePassword(password)
                .onFailure { validationErrorBuilder.addError("password", it.message!!) }
        }
    }

    private fun validatePassword(password: String) = runCatching {
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
    }

    private fun checkValidationResult(validationErrorBuilder: ValidationException.ValidationErrorBuilder): Result<Unit> {
        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
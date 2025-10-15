package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.AccountUpdateDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.apache.commons.validator.routines.EmailValidator
import org.apache.tika.utils.StringUtils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Locale

private const val EMAIL_INVALID_KEY = "${MYCV_KEY_PREFIX}.validations.email"
private const val ACCOUNT_VALIDATION_KEY_PREFIX = "${MYCV_KEY_PREFIX}.account.validations"
private const val USERNAME_TAKEN_ERROR_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.usernameAlreadyTaken"
private const val EMAIL_TAKEN_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.emailAlreadyTaken"
private const val PHONE_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.phoneInvalid"
private const val EMPTY_HOUSE_NUMBER_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.houseNumberEmpty"
private const val COUNTRY_LENGTH_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.countryLengthError"
private const val COUNTRY_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.countryInvalid"
private const val LANGUAGE_INVALID_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.languageInvalid"
private const val LANGUAGE_LENGTH_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.languageLengthError"
private const val VALIDATION_ERROR_KEY = "${ACCOUNT_VALIDATION_KEY_PREFIX}.error"

private const val USERNAME_FIELD_KEY = "username"
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
private const val LANGUAGE_FIELD_KEY = "language"

@Service
class ApplicantAccountValidationService(
    private val applicantAccountRepository: ApplicantAccountRepository,
    private val messagesService: MessagesService
) {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    fun validateAccountUpdate(accountId: Long, accountUpdate: AccountUpdateDto): Result<Unit> {
        val validationErrorBuilder = ValidationException.ValidationErrorBuilder()

        validateUsername(accountUpdate.username, accountId, validationErrorBuilder)
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
        validateLanguage(accountUpdate.language, validationErrorBuilder)

        return checkValidationResult(validationErrorBuilder)
    }

    fun validateUsername(
        username: String?,
        updatedId: Long?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (username.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(USERNAME_FIELD_KEY)
            validationErrorBuilder.addError(USERNAME_FIELD_KEY, error)
        } else if (username.length > USERNAME_MAX_LENGTH) {
            val error = messagesService.fieldMaxLengthExceededError(USERNAME_FIELD_KEY, USERNAME_MAX_LENGTH)
            validationErrorBuilder.addError(USERNAME_FIELD_KEY, error)
        } else {
            val accountWithSameUsername = applicantAccountRepository.findByUsername(username).orElse(null)
            if (accountWithSameUsername != null && accountWithSameUsername.id != updatedId) {
                val error = messagesService.getMessage(USERNAME_TAKEN_ERROR_KEY)
                validationErrorBuilder.addError(USERNAME_FIELD_KEY, error)
            }
        }
    }

    fun validateFirstName(
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

    fun validateLastName(
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

    fun validateEmail(
        email: String?,
        updateId: Long?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder,
    ) {
        when {
            email.isNullOrBlank() -> {
                val error = messagesService.requiredFieldMissingError(EMAIL_FIELD_KEY)
                validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
            }

            !EmailValidator.getInstance().isValid(email) -> {
                val error = messagesService.getMessage(EMAIL_INVALID_KEY)
                validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
            }

            email.length > EMAIL_MAX_LENGTH -> {
                val error = messagesService.fieldMaxLengthExceededError(EMAIL_FIELD_KEY, EMAIL_MAX_LENGTH)
                validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
            }

            else -> {
                val applicant = applicantAccountRepository.findByEmail(email)
                if (applicant.isPresent && applicant.get().id != updateId) {
                    val error = messagesService.getMessage(EMAIL_TAKEN_KEY)
                    validationErrorBuilder.addError(EMAIL_FIELD_KEY, error)
                }
            }
        }
    }

    fun validateBirthday(
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

    fun validatePhone(
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

    fun validateStreet(
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

    fun validateHouseNumber(
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

    fun validatePostcode(
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

    fun validateCity(
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

    fun validateCountry(
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        when {
            country.isNullOrBlank() -> {
                val error = messagesService.requiredFieldMissingError(COUNTRY_FIELD_KEY)
                validationErrorBuilder.addError(COUNTRY_FIELD_KEY, error)
            }

            country.length != COUNTRY_LENGTH -> {
                val error = messagesService.getMessage(COUNTRY_LENGTH_KEY, COUNTRY_LENGTH.toString())
                validationErrorBuilder.addError(COUNTRY_FIELD_KEY, error)
            }

            !phoneNumberUtil.supportedRegions.contains(country) -> {
                val error = messagesService.getMessage(COUNTRY_INVALID_KEY)
                validationErrorBuilder.addError(COUNTRY_FIELD_KEY, error)
            }
        }
    }

    fun validateLanguage(language: String?, validationErrorBuilder: ValidationException.ValidationErrorBuilder) {
        if (language.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(LANGUAGE_FIELD_KEY)
            validationErrorBuilder.addError(LANGUAGE_FIELD_KEY, error)
        } else if (language.length != LANGUAGE_LENGTH) {
            val error = messagesService.getMessage(LANGUAGE_LENGTH_KEY, LANGUAGE_LENGTH.toString())
            validationErrorBuilder.addError(LANGUAGE_FIELD_KEY, error)
        } else if (messagesService.getSupportedLanguages().contains(language).not()) {
            val error = messagesService.getMessage(LANGUAGE_INVALID_KEY)
            validationErrorBuilder.addError(LANGUAGE_FIELD_KEY, error)
        }
    }

    private fun checkValidationResult(validationErrorBuilder: ValidationException.ValidationErrorBuilder): Result<Unit> {
        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build(messagesService.getMessage(VALIDATION_ERROR_KEY)))
        }
        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.account

import ch.streckeisen.mycv.backend.account.dto.SignupRequestDto
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Locale

@Service
class ApplicantAccountValidationService(
    private val applicantAccountRepository: ApplicantAccountRepository,
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
            validationErrorBuilder.addError("firstName", "First name must not be blank")
        } else if (firstName.length > FIRST_NAME_MAX_LENGTH) {
            validationErrorBuilder.addError("firstName", "First name must not exceed $FIRST_NAME_MAX_LENGTH characters")
        }
    }

    private fun validateLastName(
        lastName: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (lastName.isNullOrBlank()) {
            validationErrorBuilder.addError("lastName", "Last name must not be blank")
        } else if (lastName.length > LAST_NAME_MAX_LENGTH) {
            validationErrorBuilder.addError("lastName", "Last name must not exceed $LAST_NAME_MAX_LENGTH characters")
        }
    }

    private fun validateEmail(
        email: String?,
        updateId: Long?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder,
    ) {
        if (email.isNullOrBlank()) {
            validationErrorBuilder.addError("email", "Email must not be blank")
        } else if (!EmailValidator.getInstance().isValid(email)) {
            validationErrorBuilder.addError("email", "Email is not valid")
        } else if (email.length > EMAIL_MAX_LENGTH) {
            validationErrorBuilder.addError("email", "Email must not exceed $EMAIL_MAX_LENGTH characters")
        } else {
            val applicant = applicantAccountRepository.findByEmail(email)
            if (applicant.isPresent && applicant.get().id != updateId) {
                validationErrorBuilder.addError("email", "Email is already taken")
            }
        }
    }

    private fun validateBirthday(
        birthday: LocalDate?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (birthday == null) {
            validationErrorBuilder.addError("birthday", "Birthday must not be blank")
        } else if (birthday.isAfter(LocalDate.now())) {
            validationErrorBuilder.addError("birthday", "Birthday must be in the past")
        }
    }

    private fun validatePhone(
        phone: String?,
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (phone.isNullOrBlank()) {
            validationErrorBuilder.addError("phone", "Phone must not be blank")
        } else if (phone.length > PHONE_MAX_LENGTH) {
            validationErrorBuilder.addError("phone", "Phone must not exceed $PHONE_MAX_LENGTH characters")
        } else {
            try {
                val phoneNumber = phoneNumberUtil.parse(phone, country ?: Locale.getDefault().country)
                val isValidNumber = phoneNumberUtil.isValidNumber(phoneNumber)
                if (!isValidNumber) {
                    validationErrorBuilder.addError("phone", "Phone number is not valid")
                }
            } catch (ex: NumberParseException) {
                validationErrorBuilder.addError("phone", "Couldn't parse phone number")
            }
        }
    }

    private fun validateStreet(
        street: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (street.isNullOrBlank()) {
            validationErrorBuilder.addError("street", "Street must not be blank")
        } else if (street.length > STREET_MAX_LENGTH) {
            validationErrorBuilder.addError("street", "Street must not exceed $STREET_MAX_LENGTH characters")
        }
    }

    private fun validateHouseNumber(
        houseNumber: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (houseNumber == "") {
            validationErrorBuilder.addError("houseNumber", "House number must be null or have a value")
        } else if (houseNumber != null && houseNumber.length > HOUSE_NUMBER_MAX_LENGTH) {
            validationErrorBuilder.addError("houseNumber", "House number must not exceed $HOUSE_NUMBER_MAX_LENGTH characters")
        }
    }

    private fun validatePostcode(
        postcode: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (postcode.isNullOrBlank()) {
            validationErrorBuilder.addError("postcode", "Postcode must not be blank")
        } else if (postcode.length > POSTCODE_MAX_LENGTH) {
            validationErrorBuilder.addError("postcode", "Postcode must exceed $POSTCODE_MAX_LENGTH characters")
        }
    }

    private fun validateCity(
        city: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (city.isNullOrBlank()) {
            validationErrorBuilder.addError("city", "City must not be blank")
        } else if (city.length > CITY_MAX_LENGTH) {
            validationErrorBuilder.addError("city", "City must not exceed $CITY_MAX_LENGTH characters")
        }
    }

    private fun validateCountry(
        country: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (country.isNullOrBlank()) {
            validationErrorBuilder.addError("country", "Country must not be blank")
        } else if (country.length != COUNTRY_MAX_LENGTH) {
            validationErrorBuilder.addError("country", "Country must be ISO $COUNTRY_MAX_LENGTH character country code")
        } else if (!phoneNumberUtil.supportedRegions.contains(country)) {
            validationErrorBuilder.addError("country", "Invalid country code")
        }
    }

    private fun validatePassword(
        password: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (password.isNullOrBlank()) {
            validationErrorBuilder.addError("password", "Password must not be blank")
        } else {
            validatePassword(password)
                .onFailure {
                    validationErrorBuilder.addError(
                        "password",
                        it.message ?: "Password doesn't match criteria"
                    )
                }
        }
    }

    private fun validatePassword(password: String) = runCatching {
        require(password.length >= 8) { "Password must have at least 8 characters" }
        require(password.none { it.isWhitespace() }) { "Password must not contain whitespaces" }
        require(password.any { it.isDigit() }) { "Password must contain at least one digit" }
        require(password.any { it.isUpperCase() }) { "Password must contain at least one uppercase letter" }
        require(password.any { it.isLowerCase() }) { "Password must contain at least one lowercase letter" }
        require(password.any { !it.isLetterOrDigit() }) { "Password must contain at least one special character" }
    }

    private fun checkValidationResult(validationErrorBuilder: ValidationException.ValidationErrorBuilder): Result<Unit> {
        if (validationErrorBuilder.hasErrors()) {
            return Result.failure(validationErrorBuilder.build("Validation failed with errors"))
        }
        return Result.success(Unit)
    }
}
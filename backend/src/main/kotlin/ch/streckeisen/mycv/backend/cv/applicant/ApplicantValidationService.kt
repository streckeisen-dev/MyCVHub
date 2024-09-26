package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.stereotype.Service

@Service
class ApplicantValidationService(
    private val applicantRepository: ApplicantRepository,
) {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    /*fun validateAddress(
        validationErrorBuilder: ValidationException.ValidationErrorBuilder,
        address: AddressDto
    ): ValidationException.ValidationErrorBuilder {
        if (address.street.isNullOrBlank()) {
            validationErrorBuilder.addError("street", "Street must not be blank")
        }

        if (address.number.isNullOrBlank()) {
            validationErrorBuilder.addError("number", "Number must not be blank")
        }

        if (address.postcode.isNullOrBlank()) {
            validationErrorBuilder.addError("postcode", "Postcode must not be blank")
        }

        if (address.city.isNullOrBlank()) {
            validationErrorBuilder.addError("city", "City must not be blank")
        }

        if (address.country.isNullOrBlank()) {
            validationErrorBuilder.addError("country", "Country must not be blank")
        } else if (address.country.length != 2) {
            validationErrorBuilder.addError("country", "Country must be ISO 2 character country code")
        } else if (!phoneNumberUtil.supportedRegions.contains(address.country)) {
            validationErrorBuilder.addError("country", "Invalid country code")
        }
        return validationErrorBuilder
    }*/

    fun validatePassword(validationErrorBuilder: ValidationException.ValidationErrorBuilder, password: String?) {
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

    fun validateEmail(validationErrorBuilder: ValidationException.ValidationErrorBuilder, updateId: Long?, email: String?) {
        if (email.isNullOrBlank()) {
            validationErrorBuilder.addError("email", "Email must not be blank")
        } else if (!EmailValidator.getInstance().isValid(email)) {
            validationErrorBuilder.addError("email", "Email is not valid")
        } else {
            val applicant = applicantRepository.findByEmail(email)
            if (applicant.isPresent && applicant.get().id != updateId) {
                validationErrorBuilder.addError("email", "Email is already taken")
            }
        }
    }

    private fun validatePassword(password: String) = runCatching {
        require(password.length >= 8) { "Password must have at least 8 characters" }
        require(password.none { it.isWhitespace() }) { "Password must not contain whitespaces" }
        require(password.any { it.isDigit() }) { "Password must contain at least one digit" }
        require(password.any { it.isUpperCase() }) { "Password must contain at least one uppercase letter" }
        require(password.any { it.isLowerCase() }) { "Password must contain at least one lowercase letter" }
        require(password.any { it.isLetterOrDigit() }) { "Password must contain at least one special character" }
    }
}
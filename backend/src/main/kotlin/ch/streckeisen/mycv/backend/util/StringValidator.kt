package ch.streckeisen.mycv.backend.util

import ch.streckeisen.mycv.backend.application.JOB_TITLE_MAX_LENGTH
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MessagesService
import org.springframework.stereotype.Service

@Service
class StringValidator(
    private val messagesService: MessagesService
) {
    fun validateRequiredString(
        requiredField: String,
        value: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        return validateRequiredString(requiredField, value, null, validationErrorBuilder)
    }

    fun validateRequiredString(
        requiredField: String,
        value: String?,
        maxLength: Int?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        val messageFieldKey = requiredField.split(".").last()
        if (value.isNullOrBlank()) {
            val error = messagesService.requiredFieldMissingError(messageFieldKey)
            validationErrorBuilder.addError(requiredField, error)
        } else if (maxLength != null && value.length > maxLength) {
            val error = messagesService.fieldMaxLengthExceededError(messageFieldKey, JOB_TITLE_MAX_LENGTH)
            validationErrorBuilder.addError(requiredField, error)
        }
    }

    fun validateOptionalString(
        optionalField: String,
        value: String?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        if (value == "") {
            val error = messagesService.eitherNullOrValue(optionalField)
            validationErrorBuilder.addError(optionalField, error)
        }
    }

    fun validateOptionalString(
        optionalField: String,
        value: String?,
        maxLength: Int?,
        validationErrorBuilder: ValidationException.ValidationErrorBuilder
    ) {
        val messageFieldKey = optionalField.split(".").last()
        if (value == "") {
            val error = messagesService.eitherNullOrValue(messageFieldKey)
            validationErrorBuilder.addError(optionalField, error)
        } else if (maxLength != null && !value.isNullOrBlank() && value.length > maxLength) {
            val error = messagesService.fieldMaxLengthExceededError(messageFieldKey, maxLength)
            validationErrorBuilder.addError(optionalField, error)
        }
    }
}
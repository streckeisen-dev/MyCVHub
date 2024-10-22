package ch.streckeisen.mycv.backend.locale

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

const val MYCV_KEY_PREFIX = "ch.streckeisen.mycv"
const val DATA_FIELD_KEY_PREFIX = "${MYCV_KEY_PREFIX}.data.fields"
const val VALIDATION_ERROR_KEY_PREFIX = "${MYCV_KEY_PREFIX}.data.validation"
const val VALIDATION_REQUIRED_ERROR_KEY = "${VALIDATION_ERROR_KEY_PREFIX}.requiredError"
const val VALIDATION_MAX_LENGTH_EXCEEDED_ERROR_KEY = "${VALIDATION_ERROR_KEY_PREFIX}.maxLengthExceeded"
const val VALIDATION_DATE_IS_AFTER_ERROR_KEY = "${VALIDATION_ERROR_KEY_PREFIX}.isAfterError"
const val VALIDATION_DATE_IN_FUTURE_ERROR_KEY = "${VALIDATION_ERROR_KEY_PREFIX}.dateInFutureError"
const val VALIDATION_NUMBER_TOO_SMALL_ERROR_KEY = "${VALIDATION_ERROR_KEY_PREFIX}.numberTooSmall"
const val VALIDATION_NUMBER_TOO_BIG_ERROR_KEY = "${VALIDATION_ERROR_KEY_PREFIX}.numberTooBig"

@Service
class MessagesService(
    private val messageSource: MessageSource
) {
    fun getMessage(key: String, vararg args: String): String {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale())
    }

    fun dataFieldName(fieldName: String): String {
        return getMessage("${DATA_FIELD_KEY_PREFIX}.${fieldName}")
    }

    fun requiredFieldMissingError(fieldName: String): String {
        return getMessage(VALIDATION_REQUIRED_ERROR_KEY, dataFieldName(fieldName))
    }

    fun fieldMaxLengthExceededError(fieldName: String, maxLength: Int): String {
        return getMessage(VALIDATION_MAX_LENGTH_EXCEEDED_ERROR_KEY, dataFieldName(fieldName), maxLength.toString())
    }

    fun dateIsAfterError(earlierDateField: String, laterDateField: String): String {
        return getMessage(
            VALIDATION_DATE_IS_AFTER_ERROR_KEY,
            dataFieldName(earlierDateField),
            dataFieldName(laterDateField)
        )
    }

    fun dateIsInFutureError(dateField: String): String {
        return getMessage(VALIDATION_DATE_IN_FUTURE_ERROR_KEY, dataFieldName(dateField))
    }

    fun numberTooSmallError(field: String, min: Int): String {
        return getMessage(VALIDATION_NUMBER_TOO_SMALL_ERROR_KEY, field, min.toString())
    }

    fun numberTooBigError(field: String, max: Int): String {
        return getMessage(VALIDATION_NUMBER_TOO_BIG_ERROR_KEY, field, max.toString())
    }
}
package ch.streckeisen.mycv.backend.exceptions

class ValidationException private constructor(message: String, val errors: Map<String, String>) : Exception(message) {
    class ValidationErrorBuilder {
        private val errors: MutableMap<String, String> = mutableMapOf()

        fun addError(field: String, message: String) {
            errors[field] = message
        }

        fun hasErrors(): Boolean {
            return errors.isNotEmpty()
        }

        fun build(message: String): ValidationException {
            return ValidationException(message, errors)
        }
    }
}


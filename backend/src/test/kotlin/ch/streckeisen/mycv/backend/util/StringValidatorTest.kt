package ch.streckeisen.mycv.backend.util

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StringValidatorTest {
    private lateinit var validator: StringValidator
    private lateinit var validationErrorBuilder: ValidationException.ValidationErrorBuilder

    @BeforeEach
    fun setup() {
        validator = StringValidator(mockk(relaxed = true))
        validationErrorBuilder = ValidationException.ValidationErrorBuilder()
    }

    @Test
    fun testValidateRequiredStringWithoutValue() {
        validator.validateRequiredString("field", null, validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateRequiredStringWithEmptyValue() {
        validator.validateRequiredString("field", "", validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidateRequiredStringWithValidValue() {
        validator.validateRequiredString("field", "value", validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidationRequiredStringWithMaxLengthAndValidValue() {
        validator.validateRequiredString("field", "a".repeat(45), 50, validationErrorBuilder)

        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidationRequiredStringWithMaxLengthAndInvalidValue() {
        validator.validateRequiredString("field", "a".repeat(45), 40, validationErrorBuilder)

        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidationRequiredStringWithMaxLengthAndValueWithMaxLength() {
        validator.validateRequiredString("field", "aaaa", 4, validationErrorBuilder)
        assertFalse { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidationRequiredStringWithMaxLengthAndValueWithMaxLengthPlusOne() {
        validator.validateRequiredString("field", "aaaaa", 4, validationErrorBuilder)
        assertTrue { validationErrorBuilder.hasErrors() }
    }

    @Test
    fun testValidationRequiredStringWithMaxLengthAndValueWithMaxLengthMinusOne() {
        validator.validateRequiredString("field", "aaaa", 5, validationErrorBuilder)
        assertFalse { validationErrorBuilder.hasErrors() }
    }
}
package ch.streckeisen.mycv.backend.util

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

fun <T> executeParameterizedTest(actual: T, isValid: Boolean, numberOfErrors: Int, validate: (T) -> Result<Unit>) {
    val result = validate(actual)
    assertValidationResult(result, isValid, numberOfErrors)
}

fun assertValidationResult(result: Result<Unit>, isValid: Boolean, numberOfErrors: Int) {
    assertEquals(isValid, result.isSuccess)
    if (!isValid) {
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is ValidationException)
        assertEquals(numberOfErrors, (ex as ValidationException).errors.size)
    }
}
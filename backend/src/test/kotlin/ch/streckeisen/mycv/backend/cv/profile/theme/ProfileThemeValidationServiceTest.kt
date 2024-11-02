package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileThemeValidationServiceTest {
    private lateinit var profileThemeValidationService: ProfileThemeValidationService

    @BeforeEach
    fun setup() {
        profileThemeValidationService = ProfileThemeValidationService(mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("themeValidationDataProvider")
    fun testThemeValidation(themeUpdate: ProfileThemeUpdateDto, isValid: Boolean, numberOfErrors: Int) {
        val result = profileThemeValidationService.validateThemeUpdate(themeUpdate)
        if (isValid) {
            assertTrue { result.isSuccess }
        } else {
            assertTrue { result.isFailure }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is ValidationException)
            assertEquals(numberOfErrors, ex.errors.size)
        }
    }

    companion object {
        @JvmStatic
        fun themeValidationDataProvider() = listOf(
            Arguments.of(
                ProfileThemeUpdateDto(null, null),
                false,
                2
            ),
            Arguments.of(
                ProfileThemeUpdateDto("test", null),
                false,
                2
            ),
            Arguments.of(
                ProfileThemeUpdateDto("#0000000", null),
                false,
                2
            ),
            Arguments.of(
                ProfileThemeUpdateDto("#000000", null),
                false,
                1
            ),
            Arguments.of(
                ProfileThemeUpdateDto(null, "test"),
                false,
                2
            ),
            Arguments.of(
                ProfileThemeUpdateDto(null, "#123ABC"),
                false,
                1
            ),
            Arguments.of(
                ProfileThemeUpdateDto("#CBA321", "#123ABC"),
                true,
                0
            )
        )
    }
}
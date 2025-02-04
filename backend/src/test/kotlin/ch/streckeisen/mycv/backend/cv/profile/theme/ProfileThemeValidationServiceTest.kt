package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.util.executeParameterizedTest
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ProfileThemeValidationServiceTest {
    private lateinit var profileThemeValidationService: ProfileThemeValidationService

    @BeforeEach
    fun setup() {
        profileThemeValidationService = ProfileThemeValidationService(mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("themeValidationDataProvider")
    fun testThemeValidation(themeUpdate: ProfileThemeUpdateDto, isValid: Boolean, numberOfErrors: Int) {
        executeParameterizedTest(
            themeUpdate,
            isValid,
            numberOfErrors
        ) { profileThemeValidationService.validateThemeUpdate(it) }
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
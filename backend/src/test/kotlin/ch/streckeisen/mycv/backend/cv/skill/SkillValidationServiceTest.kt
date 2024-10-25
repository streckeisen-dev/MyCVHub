package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SkillValidationServiceTest {
    private lateinit var skillValidationService: SkillValidationService

    @BeforeEach
    fun setup() {
        skillValidationService = SkillValidationService(mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("skillValidationDataProvider")
    fun testValidateSkill(skillUpdate: SkillUpdateDto, isValid: Boolean, numberOfErrors: Int) {
        val result = skillValidationService.validateSkill(skillUpdate)

        assertEquals(isValid, result.isSuccess)
        if (!isValid) {
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is ValidationException)
            assertEquals(numberOfErrors, ex.errors.size)
        }
    }

    companion object {
        @JvmStatic
        fun skillValidationDataProvider() = listOf(
            Arguments.of(
                SkillUpdateDto(null, null, null, null),
                false,
                3
            ),
            Arguments.of(
                SkillUpdateDto(null, "n".repeat(NAME_MAX_LENGTH + 1), null, null),
                false,
                3
            ),
            Arguments.of(
                SkillUpdateDto(null, "name", null, null),
                false,
                2
            ),
            Arguments.of(
                SkillUpdateDto(null, null, "t".repeat(TYPE_MAX_LENGTH + 1), null),
                false,
                3
            ),
            Arguments.of(
                SkillUpdateDto(null, null, "type", null),
                false,
                2
            ),
            Arguments.of(
                SkillUpdateDto(null, null, null, (LEVEL_MIN_VALUE - 1).toShort()),
                false,
                3
            ),
            Arguments.of(
                SkillUpdateDto(null, null, null, (LEVEL_MAX_VALUE + 1).toShort()),
                false,
                3
            ),
            Arguments.of(
                SkillUpdateDto(null, null, null, (LEVEL_MAX_VALUE - 1).toShort()),
                false,
                2
            ),
            Arguments.of(
                SkillUpdateDto(null, "name", "type", (LEVEL_MAX_VALUE - 1).toShort()),
                true,
                0
            )
        )
    }
}
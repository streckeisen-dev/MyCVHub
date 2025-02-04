package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.util.assertValidationResult
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.web.multipart.MultipartFile

class ProfileValidationServiceTest {
    private lateinit var profileValidationService: ProfileValidationService

    @BeforeEach
    fun setup() {
        profileValidationService = ProfileValidationService(mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("profileValidationDataProvider")
    fun testProfileValidation(
        update: GeneralProfileInformationUpdateDto,
        picture: MultipartFile?,
        isNew: Boolean,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        val result = profileValidationService.validateProfileInformation(update, picture, isNew)

        assertValidationResult(result, isValid, numberOfErrors)
    }

    companion object {
        @JvmStatic
        fun profileValidationDataProvider() = listOf(
            Arguments.of(
                GeneralProfileInformationUpdateDto(null, null, null, null, null, null, null),
                null,
                true,
                false,
                2
            ),
            Arguments.of(
                GeneralProfileInformationUpdateDto(null, null, null, null, null, null, null),
                mockk<MultipartFile>(),
                true,
                false,
                1
            ),
            Arguments.of(
                GeneralProfileInformationUpdateDto(null, null, null, null, null, null, null),
                null,
                false,
                false,
                1
            ),
            Arguments.of(
                GeneralProfileInformationUpdateDto(
                    "j".repeat(JOB_TITLE_MAX_LENGTH + 1),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                null,
                false,
                false,
                1
            ),
            Arguments.of(
                GeneralProfileInformationUpdateDto("jobTitle", null, null, null, null, null, null),
                null,
                true,
                false,
                1
            ),
            Arguments.of(
                GeneralProfileInformationUpdateDto(
                    null,
                    "my\nbio",
                    isProfilePublic = false,
                    isEmailPublic = false,
                    isPhonePublic = true,
                    isAddressPublic = false,
                    hideDescriptions = true
                ),
                null,
                false,
                false,
                1
            ),
            Arguments.of(
                GeneralProfileInformationUpdateDto(
                    "job",
                    null,
                    isProfilePublic = false,
                    isEmailPublic = false,
                    isPhonePublic = true,
                    isAddressPublic = false,
                    hideDescriptions = true
                ),
                null,
                false,
                true,
                0
            )
        )
    }
}
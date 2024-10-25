package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.web.multipart.MultipartFile
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val existingProfile =
    ProfileEntity(
        "existing",
        "job",
        "bio",
        isProfilePublic = false,
        isEmailPublic = false,
        isPhonePublic = false,
        isAddressPublic = false,
        hideDescriptions = true,
        profilePicture = "picture.jpg",
        id = 1,
        account = mockk {
            every { id } returns 1
        }
    )

class ProfileValidationServiceTest {
    private lateinit var profileRepository: ProfileRepository
    private lateinit var profileValidationService: ProfileValidationService

    @BeforeEach
    fun setup() {
        profileRepository = mockk {
            every { findByAlias(any()) } returns Optional.empty()
            every { findByAlias(eq("existing")) } returns Optional.of(existingProfile)
        }
        profileValidationService = ProfileValidationService(profileRepository, mockk(relaxed = true))
    }

    @ParameterizedTest
    @MethodSource("aliasValidationDataProvider")
    fun testAliasValidation(alias: String, isValid: Boolean) {
        val validationResult = aliasRegex.matches(alias)
        assertEquals(isValid, validationResult)
    }

    @ParameterizedTest
    @MethodSource("profileValidationDataProvider")
    fun testProfileValidation(
        updateAccountId: Long,
        update: GeneralProfileInformationUpdateDto,
        picture: MultipartFile?,
        isNew: Boolean,
        isValid: Boolean,
        numberOfErrors: Int
    ) {
        val result = profileValidationService.validateProfileInformation(updateAccountId, update, picture, isNew)

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
        fun aliasValidationDataProvider() = listOf(
            Arguments.of("alias", true),
            Arguments.of("a-lias", true),
            Arguments.of("a_lias", true),
            Arguments.of("al1as", true),
            Arguments.of("alas0", true),
            Arguments.of("alias-", false),
            Arguments.of("alias_", false),
            Arguments.of("0alias", false),
            Arguments.of("-alias", false),
            Arguments.of("al%ias", false),
        )

        @JvmStatic
        fun profileValidationDataProvider() = listOf(
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(null, null, null, null, null, null, null, null),
                null,
                true,
                false,
                3
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(null, null, null, null, null, null, null, null),
                mockk<MultipartFile>(),
                true,
                false,
                2
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(null, null, null, null, null, null, null, null),
                null,
                false,
                false,
                2
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(
                    "a".repeat(ALIAS_MAX_LENGTH + 1),
                    null,
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
                2
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto("a b", null, null, null, null, null, null, null),
                null,
                false,
                false,
                2
            ),
            Arguments.of(
                2,
                GeneralProfileInformationUpdateDto("existing", null, null, null, null, null, null, null),
                null,
                false,
                false,
                2
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto("alias", null, null, null, null, null, null, null),
                null,
                false,
                false,
                1
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto("existing", null, null, null, null, null, null, null),
                null,
                false,
                false,
                1
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(
                    null,
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
                2
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(null, "jobTitle", null, null, null, null, null, null),
                null,
                false,
                false,
                1
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(
                    null,
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
                2
            ),
            Arguments.of(
                1,
                GeneralProfileInformationUpdateDto(
                    "new-alias",
                    "job",
                    "my\nbio",
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
            ),
        )
    }
}
package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.account.AccountDetailsEntity
import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import kotlin.test.assertTrue

class PublicProfileMapperTest {
    @ParameterizedTest
    @MethodSource("publicProfileDataProvider")
    fun testToPublicDto(
        isEmailPublic: Boolean,
        isPhonePublic: Boolean,
        isAddressPublic: Boolean,
        hideDescriptions: Boolean
    ) {
        val profile = profileEntity(isEmailPublic, isPhonePublic, isAddressPublic, hideDescriptions)

        val publicProfile = profile.toPublicDto("")

        assertTrue { if (isEmailPublic) publicProfile.email != null else publicProfile.email == null }
        assertTrue { if (isPhonePublic) publicProfile.phone != null else publicProfile.phone == null }
        assertTrue { if (isAddressPublic) publicProfile.address != null else publicProfile.address == null }
        assertTrue {
            if (hideDescriptions) {
                publicProfile.workExperiences.all { it.description == null }
                publicProfile.education.all { it.description == null }
            } else {
                publicProfile.workExperiences.all { it.description != null }
                publicProfile.education.all { it.description != null }
            }
        }
    }

    private fun profileEntity(
        isEmailPublic: Boolean,
        isPhonePublic: Boolean,
        isAddressPublic: Boolean,
        hideDescriptions: Boolean
    ): ProfileEntity {
        return ProfileEntity(
            "j",
            null,
            true,
            isEmailPublic,
            isPhonePublic,
            isAddressPublic,
            hideDescriptions,
            "p",
            1,
            ApplicantAccountEntity(
                "u",
                "p",
                false,
                true,
                accountDetails = AccountDetailsEntity(
                    "f",
                    "l",
                    "e",
                    "p",
                    LocalDate.now(),
                    "s",
                    null,
                    "p",
                    "c",
                    "c"
                ),
                id = 1
            ),
            workExperiences = listOf(
                WorkExperienceEntity(
                    1,
                    "j",
                    "c",
                    LocalDate.now(),
                    null,
                    "l",
                    "d",
                    mockk()
                )
            ),
            education = listOf(
                EducationEntity(
                    1,
                    "i",
                    "l",
                    LocalDate.now(),
                    null,
                    "de",
                    "d",
                    mockk()
                )
            )
        )
    }

    companion object {
        @JvmStatic
        fun publicProfileDataProvider() = listOf(
            Arguments.of(false, false, false, false, false),
            Arguments.of(true, true, true, true, true)
        )
    }
}
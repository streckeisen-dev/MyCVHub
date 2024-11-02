package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.theme.ProfileThemeEntity
import ch.streckeisen.mycv.backend.cv.skill.SkillEntity
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicAddressDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicEducationDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicSkillDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileThemeDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicWorkExperienceDto

fun ProfileEntity.toPublicDto(profilePicture: String): PublicProfileDto = PublicProfileDto(
    account.firstName,
    account.lastName,
    jobTitle,
    bio,
    email = if (isEmailPublic) account.email else null,
    phone = if (isPhonePublic) account.phone else null,
    address = if (isAddressPublic) PublicAddressDto(
        account.street,
        account.houseNumber,
        account.postcode,
        account.city,
        account.country
    ) else null,
    profilePicture = profilePicture,
    workExperiences.map { it.toPublicDto(hideDescriptions) }.toList(),
    skills.map { it.toPublicDto() }.toList(),
    education.map { it.toPublicDto(hideDescriptions) }.toList(),
    profileTheme?.toPublicDto()
)

fun EducationEntity.toPublicDto(hideDescription: Boolean) = PublicEducationDto(
    institution,
    location,
    educationStart,
    educationEnd,
    degreeName,
    description = if (hideDescription) null else description
)

fun WorkExperienceEntity.toPublicDto(hideDescription: Boolean) = PublicWorkExperienceDto(
    jobTitle,
    company,
    positionStart,
    positionEnd,
    location,
    description = if (hideDescription) null else description
)

fun SkillEntity.toPublicDto() = PublicSkillDto(
    name,
    type,
    level
)

fun ProfileThemeEntity.toPublicDto() = PublicProfileThemeDto(
    backgroundColor,
    surfaceColor
)
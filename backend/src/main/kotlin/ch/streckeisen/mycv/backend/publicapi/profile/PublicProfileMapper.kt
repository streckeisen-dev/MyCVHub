package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import ch.streckeisen.mycv.backend.cv.profile.theme.ProfileThemeEntity
import ch.streckeisen.mycv.backend.cv.project.ProjectEntity
import ch.streckeisen.mycv.backend.cv.project.PublicProjectDto
import ch.streckeisen.mycv.backend.cv.project.toDto
import ch.streckeisen.mycv.backend.cv.skill.SkillEntity
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicAddressDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicEducationDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileThemeDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicSkillDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicWorkExperienceDto

fun ProfileEntity.toPublicDto(profilePicture: String): PublicProfileDto {
    val details = account.accountDetails!!
    return PublicProfileDto(
        firstName = details.firstName,
        lastName = details.lastName,
        jobTitle = jobTitle,
        bio = bio,
        email = if (isEmailPublic) details.email else null,
        phone = if (isPhonePublic) details.phone else null,
        address = if (isAddressPublic) PublicAddressDto(
            details.street,
            details.houseNumber,
            details.postcode,
            details.city,
            details.country
        ) else null,
        profilePicture = profilePicture,
        workExperiences = workExperiences.map { it.toPublicDto(hideDescriptions) }.toList(),
        skills = skills.map { it.toPublicDto() }.toList(),
        education = education.map { it.toPublicDto(hideDescriptions) }.toList(),
        projects = projects.map { it.toPublicDto(hideDescriptions) }.toList(),
        theme = profileTheme?.toPublicDto(),
        language = details.language
    )
}

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

fun ProjectEntity.toPublicDto(hideDescription: Boolean) = PublicProjectDto(
    name,
    role,
    description = if (hideDescription) null else description,
    projectStart,
    projectEnd,
    links.map { it.toDto() }
)

fun ProfileThemeEntity.toPublicDto() = PublicProfileThemeDto(
    backgroundColor,
    surfaceColor
)

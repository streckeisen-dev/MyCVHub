package ch.streckeisen.mycv.backend.publicapi.profile

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import ch.streckeisen.mycv.backend.cv.education.Education
import ch.streckeisen.mycv.backend.cv.experience.WorkExperience
import ch.streckeisen.mycv.backend.cv.skill.Skill
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicEducationDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicSkillDto
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicWorkExperienceDto

fun Applicant.toPublicDto(): PublicProfileDto = PublicProfileDto(
    id!!,
    firstName,
    lastName,
    email = if (privacySettings.isEmailPublic) email else null,
    phone = if (privacySettings.isPhonePublic) phone else null,
    birthday = if (privacySettings.isBirthdayPublic) birthday else null,
    street = if (privacySettings.isAddressPublic) street else null,
    houseNumber = if (privacySettings.isAddressPublic) houseNumber else null,
    postcode = if (privacySettings.isAddressPublic) postcode else null,
    city,
    country,
    workExperiences.map { it.toPublicDto() }.toList(),
    skills.map { it.toPublicDto() }.toList(),
    education.map { it.toPublicDto() }.toList()
)

fun Education.toPublicDto() = PublicEducationDto(
    id,
    school,
    location,
    educationStart,
    educationEnd,
    degreeName,
    description
)

fun WorkExperience.toPublicDto() = PublicWorkExperienceDto(
    id,
    company,
    positionStart,
    positionEnd,
    location,
    description
)

fun Skill.toPublicDto() = PublicSkillDto(
    id,
    name,
    type,
    level
)
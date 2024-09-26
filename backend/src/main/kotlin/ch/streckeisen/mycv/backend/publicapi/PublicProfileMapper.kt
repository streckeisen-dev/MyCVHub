package ch.streckeisen.mycv.backend.publicapi

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import ch.streckeisen.mycv.backend.cv.education.Education
import ch.streckeisen.mycv.backend.cv.experience.WorkExperience
import ch.streckeisen.mycv.backend.cv.skill.Skill

fun Applicant.toPublicDto(): PublicProfileDto = PublicProfileDto(
    id,
    firstName,
    lastName,
    email,
    phone,
    birthday,
    street,
    houseNumber,
    postcode,
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
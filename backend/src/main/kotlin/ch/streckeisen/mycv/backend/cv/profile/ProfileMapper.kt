package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.education.toDto
import ch.streckeisen.mycv.backend.cv.experience.toDto
import ch.streckeisen.mycv.backend.cv.profile.theme.toDto
import ch.streckeisen.mycv.backend.cv.skill.toDto

fun ProfileEntity.toDto(profilePicture: String) = ProfileDto(
    jobTitle,
    bio,
    isProfilePublic,
    isEmailPublic,
    isPhonePublic,
    isAddressPublic,
    hideDescriptions,
    profilePicture,
    workExperiences.map { it.toDto() }.toList(),
    education.map { it.toDto() }.toList(),
    skills.map { it.toDto() }.toList(),
    profileTheme?.toDto()
)
package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.education.toDto
import ch.streckeisen.mycv.backend.cv.experience.toDto
import ch.streckeisen.mycv.backend.cv.skill.toDto

fun ProfileEntity.toDto() = ProfileDto(
    alias,
    jobTitle,
    aboutMe,
    isProfilePublic,
    isEmailPublic,
    isPhonePublic,
    isAddressPublic,
    workExperiences.map { it.toDto() }.toList(),
    education.map { it.toDto() }.toList(),
    skills.map { it.toDto() }.toList()
)
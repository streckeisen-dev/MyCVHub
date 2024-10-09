package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.education.EducationDto
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceDto
import ch.streckeisen.mycv.backend.cv.skill.SkillDto

data class ProfileDto(
    val alias: String,
    val jobTitle: String,
    val aboutMe: String,
    val isProfilePublic: Boolean,
    val isEmailPublic: Boolean,
    val isPhonePublic: Boolean,
    val isAddressPublic: Boolean,
    val workExperiences: List<WorkExperienceDto>,
    val education: List<EducationDto>,
    val skills: List<SkillDto>
)

package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.cv.education.EducationDto
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceDto
import ch.streckeisen.mycv.backend.cv.skill.SkillDto
import com.fasterxml.jackson.annotation.JsonProperty

data class ProfileDto(
    val alias: String,
    val jobTitle: String,
    val bio: String?,
    @get:JsonProperty(value = "isProfilePublic") val isProfilePublic: Boolean,
    @get:JsonProperty(value = "isEmailPublic") val isEmailPublic: Boolean,
    @get:JsonProperty(value = "isPhonePublic") val isPhonePublic: Boolean,
    @get:JsonProperty(value = "isAddressPublic") val isAddressPublic: Boolean,
    val hideDescriptions: Boolean,
    val profilePicture: String,
    val workExperiences: List<WorkExperienceDto>,
    val education: List<EducationDto>,
    val skills: List<SkillDto>
)

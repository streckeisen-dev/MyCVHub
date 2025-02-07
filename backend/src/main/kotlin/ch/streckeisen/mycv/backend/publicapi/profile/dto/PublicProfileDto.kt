package ch.streckeisen.mycv.backend.publicapi.profile.dto

import ch.streckeisen.mycv.backend.cv.project.PublicProjectDto

data class PublicProfileDto(
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val bio: String?,
    val email: String?,
    val phone: String?,
    val address: PublicAddressDto?,
    val profilePicture: String,
    val workExperiences: List<PublicWorkExperienceDto>,
    val skills: List<PublicSkillDto>,
    val education: List<PublicEducationDto>,
    val projects: List<PublicProjectDto>,
    val theme: PublicProfileThemeDto?
)

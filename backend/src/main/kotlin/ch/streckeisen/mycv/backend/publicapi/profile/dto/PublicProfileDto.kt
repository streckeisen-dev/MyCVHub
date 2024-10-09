package ch.streckeisen.mycv.backend.publicapi.profile.dto

data class PublicProfileDto(
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val aboutMe: String,
    val email: String?,
    val phone: String?,
    val address: PublicAddressDto?,
    val workExperiences: List<PublicWorkExperienceDto>,
    val skills: List<PublicSkillDto>,
    val education: List<PublicEducationDto>
)

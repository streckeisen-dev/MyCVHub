package ch.streckeisen.mycv.backend.publicapi

import java.time.LocalDate

data class PublicProfileDto(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val birthday: LocalDate,
    val street: String,
    val houseNumber: String?,
    val postcode: String,
    val city: String,
    val country: String,
    val workExperiences: List<PublicWorkExperienceDto>,
    val skills: List<PublicSkillDto>,
    val education: List<PublicEducationDto>,
)

package ch.streckeisen.mycv.backend.cv.applicant.publicapi

import ch.streckeisen.mycv.backend.cv.applicant.AddressDto
import ch.streckeisen.mycv.backend.cv.education.EducationDto
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceDto
import ch.streckeisen.mycv.backend.cv.skill.SkillDto
import java.time.LocalDate

data class PublicApplicantDto(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val birthday: LocalDate,
    val address: AddressDto,
    var workExperiences: List<WorkExperienceDto>?,
    var skills: List<SkillDto>?,
    var education: List<EducationDto>?,
)

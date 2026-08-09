package ch.streckeisen.mycv.backend.cv.generator

import ch.streckeisen.mycv.backend.cv.project.ProjectLinkType
import java.time.LocalDate

data class CVGenerationSnapshot(
    val accountId: Long,
    val isVerified: Boolean,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    val street: String?,
    val houseNumber: String?,
    val postcode: String?,
    val city: String?,
    val birthday: LocalDate?,
    val language: String?,
    val profilePicture: String,
    val jobTitle: String,
    val bio: String?,
    val workExperiences: List<CVWorkExperienceSnapshot>,
    val education: List<CVEducationSnapshot>,
    val projects: List<CVProjectSnapshot>,
    val skills: List<CVSkillSnapshot>
)

data class CVWorkExperienceSnapshot(
    val id: Long?,
    val jobTitle: String,
    val company: String,
    val positionStart: LocalDate,
    val positionEnd: LocalDate?,
    val location: String,
    val description: String
)

data class CVEducationSnapshot(
    val id: Long?,
    val institution: String,
    val location: String,
    val educationStart: LocalDate,
    val educationEnd: LocalDate?,
    val degreeName: String,
    val description: String?
)

data class CVProjectSnapshot(
    val id: Long?,
    val name: String,
    val role: String,
    val description: String,
    val projectStart: LocalDate,
    val projectEnd: LocalDate?,
    val links: List<CVProjectLinkSnapshot>
)

data class CVProjectLinkSnapshot(
    val url: String,
    val displayName: String,
    val type: ProjectLinkType
)

data class CVSkillSnapshot(
    val id: Long?,
    val name: String,
    val type: String,
    val level: Int
)

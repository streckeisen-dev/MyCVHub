package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.cv.education.Education
import ch.streckeisen.mycv.backend.cv.experience.WorkExperience
import ch.streckeisen.mycv.backend.cv.skill.Skill
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
class Applicant(
    @Column(name = "firstname")
    val firstName: String,
    @Column(name = "lastname")
    val lastName: String,
    val email: String,
    val phone: String,
    val birthday: LocalDate,
    val street: String,
    val houseNumber: String?,
    val postcode: String,
    val city: String,
    val country: String,
    val password: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val hasPublicProfile: Boolean = true,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicant")
    val workExperiences: List<WorkExperience> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicant")
    val skills: List<Skill> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "applicant")
    val education: List<Education> = listOf(),
)
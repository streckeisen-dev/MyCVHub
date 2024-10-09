package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.skill.SkillEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class ProfileEntity(
    val alias: String,
    val jobTitle: String,
    val aboutMe: String,
    val isProfilePublic: Boolean,
    val isEmailPublic: Boolean,
    val isPhonePublic: Boolean,
    val isAddressPublic: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    val account: ApplicantAccountEntity,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    val workExperiences: List<WorkExperienceEntity> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    val skills: List<SkillEntity> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    val education: List<EducationEntity> = listOf(),
)
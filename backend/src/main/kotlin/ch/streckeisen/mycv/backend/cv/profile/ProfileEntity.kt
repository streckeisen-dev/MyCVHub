package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import ch.streckeisen.mycv.backend.cv.education.EducationEntity
import ch.streckeisen.mycv.backend.cv.experience.WorkExperienceEntity
import ch.streckeisen.mycv.backend.cv.profile.theme.ProfileThemeEntity
import ch.streckeisen.mycv.backend.cv.project.ProjectEntity
import ch.streckeisen.mycv.backend.cv.skill.SkillEntity
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
@EntityListeners(ProfileEntityDeletionListener::class)
class ProfileEntity(
    var jobTitle: String,
    var bio: String?,
    var isProfilePublic: Boolean,
    var isEmailPublic: Boolean,
    var isPhonePublic: Boolean,
    var isAddressPublic: Boolean,
    var hideDescriptions: Boolean,
    var profilePicture: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    var account: ApplicantAccountEntity,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    var workExperiences: List<WorkExperienceEntity> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    var skills: List<SkillEntity> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    var education: List<EducationEntity> = listOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "profile")
    var projects: List<ProjectEntity> = listOf(),
    @OneToOne(mappedBy = "profile")
    var profileTheme: ProfileThemeEntity? = null
)
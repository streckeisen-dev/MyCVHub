package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class WorkExperienceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var jobTitle: String,
    var company: String,
    var positionStart: LocalDate,
    var positionEnd: LocalDate?,
    var location: String,
    var description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    var profile: ProfileEntity
)
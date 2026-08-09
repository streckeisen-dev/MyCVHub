package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class EducationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var institution: String,
    var location: String,
    var educationStart: LocalDate,
    var educationEnd: LocalDate?,
    var degreeName: String,
    var description: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    var profile: ProfileEntity
)
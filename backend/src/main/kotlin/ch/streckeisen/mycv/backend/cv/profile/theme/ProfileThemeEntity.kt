package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class ProfileThemeEntity(
    var backgroundColor: String,
    var surfaceColor: String,
    @OneToOne
    var profile: ProfileEntity,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)
package ch.streckeisen.mycv.backend.cv.profile.theme

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class ProfileThemeEntity(
    val backgroundColor: String,
    val surfaceColor: String,
    @OneToOne
    val profile: ProfileEntity,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)
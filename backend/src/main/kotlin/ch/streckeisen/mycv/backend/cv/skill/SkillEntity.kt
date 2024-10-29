package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class SkillEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,
    val type: String,
    val level: Short,

    @ManyToOne(fetch = FetchType.LAZY)
    val profile: ProfileEntity
)
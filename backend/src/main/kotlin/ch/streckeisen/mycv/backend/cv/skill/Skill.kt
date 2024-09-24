package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Skill(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,
    @Enumerated(EnumType.STRING)
    val type: SkillType,
    val level: Short,

    @ManyToOne(fetch = FetchType.LAZY)
    val applicant: Applicant
)
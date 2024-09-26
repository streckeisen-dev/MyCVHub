package ch.streckeisen.mycv.backend.publicapi

import ch.streckeisen.mycv.backend.cv.skill.SkillType

data class PublicSkillDto(
    val id: Long?,
    val name: String,
    val type: SkillType,
    val level: Short
)
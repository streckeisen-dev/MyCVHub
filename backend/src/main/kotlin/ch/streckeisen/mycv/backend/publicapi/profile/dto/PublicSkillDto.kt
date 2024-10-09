package ch.streckeisen.mycv.backend.publicapi.profile.dto

import ch.streckeisen.mycv.backend.cv.skill.SkillType

data class PublicSkillDto(
    val name: String,
    val type: SkillType,
    val level: Short
)
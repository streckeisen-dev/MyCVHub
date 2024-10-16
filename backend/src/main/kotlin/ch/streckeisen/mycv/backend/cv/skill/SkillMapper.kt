package ch.streckeisen.mycv.backend.cv.skill

fun SkillEntity.toDto() = SkillDto(
    id!!,
    name,
    type,
    level
)


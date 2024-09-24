package ch.streckeisen.mycv.backend.cv.skill

data class SkillDto(
    val id: Long?,
    val name: String,
    val type: SkillType,
    val level: Short,
    val applicantId: Long
)


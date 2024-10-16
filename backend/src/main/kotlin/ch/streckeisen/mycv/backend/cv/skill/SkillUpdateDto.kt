package ch.streckeisen.mycv.backend.cv.skill

data class SkillUpdateDto(
    val id: Long?,
    val name: String?,
    val type: String?,
    val level: Short?
)

package ch.streckeisen.mycv.backend.cv.project

data class ProjectLinkDto(
    val url: String,
    val displayName: String,
    val type: ProjectLinkType
)

package ch.streckeisen.mycv.backend.cv.project

data class ProjectLinkUpdateDto(
    val url: String?,
    val displayName: String?,
    val type: ProjectLinkType?
)

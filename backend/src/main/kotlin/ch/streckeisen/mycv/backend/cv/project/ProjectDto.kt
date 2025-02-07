package ch.streckeisen.mycv.backend.cv.project

import java.time.LocalDate

data class ProjectDto(
    val id: Long,
    val name: String,
    val role: String,
    val description: String,
    val projectStart: LocalDate,
    val projectEnd: LocalDate?,
    val links: List<ProjectLinkDto>
)
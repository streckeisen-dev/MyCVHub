package ch.streckeisen.mycv.backend.cv.project

fun ProjectEntity.toDto() = ProjectDto(
    id = id!!,
    name = name,
    role = role,
    description = description,
    projectStart = projectStart,
    projectEnd = projectEnd,
    links = links.map { it.toDto() }
)

fun ProjectLink.toDto() = ProjectLinkDto(
    url = url,
    type = type
)
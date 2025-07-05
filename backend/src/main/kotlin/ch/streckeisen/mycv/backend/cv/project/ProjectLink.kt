package ch.streckeisen.mycv.backend.cv.project

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class ProjectLink(
    val url: String,
    val displayName: String,
    @Enumerated(EnumType.STRING)
    val type: ProjectLinkType
)

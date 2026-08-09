package ch.streckeisen.mycv.backend.cv.project

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class ProjectLink(
    var url: String,
    var displayName: String,
    @Enumerated(EnumType.STRING)
    var type: ProjectLinkType
)

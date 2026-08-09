package ch.streckeisen.mycv.backend.cv.project

import ch.streckeisen.mycv.backend.cv.profile.ProfileEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class ProjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    var role: String,
    var description: String,
    var projectStart: LocalDate,
    var projectEnd: LocalDate?,
    @ElementCollection
    @CollectionTable(name = "project_links", joinColumns = [JoinColumn(name = "project_id")])
    var links: List<ProjectLink> = listOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    var profile: ProfileEntity
)
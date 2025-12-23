package ch.streckeisen.mycv.backend.cv.project

import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<ProjectEntity, Long>
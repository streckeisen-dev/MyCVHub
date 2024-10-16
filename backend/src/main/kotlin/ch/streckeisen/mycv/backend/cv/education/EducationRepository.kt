package ch.streckeisen.mycv.backend.cv.education

import org.springframework.data.repository.CrudRepository

interface EducationRepository : CrudRepository<EducationEntity, Long> {
}
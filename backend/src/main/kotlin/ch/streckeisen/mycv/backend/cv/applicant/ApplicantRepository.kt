package ch.streckeisen.mycv.backend.cv.applicant

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface ApplicantRepository: CrudRepository<Applicant, Long> {
    fun findByEmail(email: String): Optional<Applicant>
}
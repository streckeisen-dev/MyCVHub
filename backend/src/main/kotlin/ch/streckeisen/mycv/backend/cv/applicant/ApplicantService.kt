package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicantService(
    private val applicantRepository: ApplicantRepository
) {
    @Transactional(readOnly = true)
    fun getById(id: Long): Result<Applicant> {
        return applicantRepository.findById(id)
            .map { applicant -> Result.success(applicant) }
            .orElse(Result.failure(ResultNotFoundException("No applicant with ID $id")))
    }

    @Transactional(readOnly = false)
    fun create(applicant: Applicant): Applicant {
        return applicantRepository.save(applicant)
    }
}
package ch.streckeisen.mycv.backend.cv.applicant

import ch.streckeisen.mycv.backend.cv.applicant.publicapi.PublicApplicantDto
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import org.springframework.stereotype.Service

@Service
class ApplicantService(
    private val applicantRepository: ApplicantRepository
) {
    fun getById(id: Long): Result<PublicApplicantDto> {
        return applicantRepository.findById(id)
            .map { applicant -> Result.success(applicant.toDto()) }
            .orElse(Result.failure(ResultNotFoundException("No applicant with ID $id")))
    }

    fun create(applicant: Applicant): PublicApplicantDto {
        return applicantRepository.save(applicant).toDto()
    }
}
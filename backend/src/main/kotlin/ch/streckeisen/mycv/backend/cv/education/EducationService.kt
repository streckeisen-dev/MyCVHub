package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class EducationService(
    private val educationRepository: EducationRepository,
    private val educationValidationService: EducationValidationService,
    private val profileService: ProfileService
) {
    @Transactional
    fun save(accountId: Long, educationUpdate: EducationUpdateDto): Result<EducationEntity> {
        val existingEducation = if (educationUpdate.id != null) {
            educationRepository.findById(educationUpdate.id)
                .getOrElse { return Result.failure(ResultNotFoundException("This eduction entry does not exist")) }
        } else null

        if (existingEducation != null && existingEducation.profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You don't have permission to modify this education entry"))
        }

        val profile = if (existingEducation != null) {
            existingEducation.profile
        } else {
            profileService.findByAccountId(accountId)
                .onFailure { return Result.failure(it) }
                .getOrNull()!!
        }
        educationValidationService.validateEducation(educationUpdate)
            .onFailure { return Result.failure(it) }

        return Result.success(
            educationRepository.save(
                EducationEntity(
                    existingEducation?.id,
                    educationUpdate.institution!!,
                    educationUpdate.location!!,
                    educationUpdate.educationStart!!,
                    educationUpdate.educationEnd,
                    educationUpdate.degreeName!!,
                    educationUpdate.description!!,
                    profile
                )
            )
        )
    }

    @Transactional
    fun delete(accountId: Long, id: Long): Result<Unit> {
        val education = educationRepository.findById(id)
            .getOrElse { return Result.failure(ResultNotFoundException("This eduction entry does not exist")) }

        if (education.profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You don't have permission to delete this education entry"))
        }

        educationRepository.delete(education)
        return Result.success(Unit)
    }
}
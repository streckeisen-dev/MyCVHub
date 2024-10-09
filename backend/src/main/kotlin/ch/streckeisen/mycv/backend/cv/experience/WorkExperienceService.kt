package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class WorkExperienceService(
    private val workExperienceRepository: WorkExperienceRepository,
    private val workExperienceValidationService: WorkExperienceValidationService,
    private val profileService: ProfileService
) {
    @Transactional
    fun save(applicantId: Long, workExperience: WorkExperienceUpdateRequestDto): Result<WorkExperienceEntity> {
        val validationResult = workExperienceValidationService.validateWorkExperience(workExperience)
        validationResult.onFailure { return Result.failure(it) }

        val existingWorkExperience = if (workExperience.id != null) {
            workExperienceRepository.findById(workExperience.id).orElse(null)
        } else null

        if (existingWorkExperience != null && existingWorkExperience.profile.account.id != applicantId) {
            return Result.failure(AccessDeniedException("You don't have permission to update this work experience"))
        }

        val profile = if (existingWorkExperience != null) {
            existingWorkExperience.profile
        } else {
            val profileResult = profileService.findByAccountId(applicantId)
            profileResult.onFailure { return Result.failure(it) }
            profileResult.getOrNull()!!
        }

        return Result.success(
            workExperienceRepository.save(
                WorkExperienceEntity(
                    existingWorkExperience?.id,
                    workExperience.jobTitle!!,
                    workExperience.company!!,
                    workExperience.positionStart!!,
                    workExperience.positionEnd,
                    workExperience.location!!,
                    workExperience.description!!,
                    profile
                )
            )
        )
    }

    @Transactional
    fun delete(applicantId: Long, workExperienceId: Long): Result<Unit> {
        val workExperience = workExperienceRepository.findById(workExperienceId).getOrElse {
            return Result.failure(IllegalArgumentException("This work experience does not exist"))
        }

        if (applicantId != workExperience.profile.account.id) {
            return Result.failure(AccessDeniedException("You don't have permission to delete this work experience"))
        }

        workExperienceRepository.delete(workExperience)
        return Result.success(Unit)
    }
}
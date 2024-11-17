package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
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
    fun save(applicantId: Long, workExperience: WorkExperienceUpdateDto): Result<WorkExperienceEntity> {
        val existingWorkExperience = if (workExperience.id != null) {
            workExperienceRepository.findById(workExperience.id)
                .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.workExperience.notFound")) }
        } else null

        if (existingWorkExperience != null && existingWorkExperience.profile.account.id != applicantId) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.workExperience.accessDenied"))
        }

        val profile = if (existingWorkExperience != null) {
            existingWorkExperience.profile
        } else {
            profileService.findByAccountId(applicantId)
                .onFailure { return Result.failure(it) }
                .getOrNull()!!
        }

        workExperienceValidationService.validateWorkExperience(workExperience)
            .onFailure { return Result.failure(it) }

        return Result.success(
            workExperienceRepository.save(
                WorkExperienceEntity(
                    id = existingWorkExperience?.id,
                    jobTitle = workExperience.jobTitle!!,
                    company = workExperience.company!!,
                    positionStart = workExperience.positionStart!!,
                    positionEnd = workExperience.positionEnd,
                    location = workExperience.location!!,
                    description = workExperience.description!!,
                    profile = profile
                )
            )
        )
    }

    @Transactional
    fun delete(applicantId: Long, workExperienceId: Long): Result<Unit> {
        val workExperience = workExperienceRepository.findById(workExperienceId).getOrElse {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.workExperience.notFound"))
        }

        if (applicantId != workExperience.profile.account.id) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.workExperience.accessDenied"))
        }

        workExperienceRepository.delete(workExperience)
        return Result.success(Unit)
    }
}
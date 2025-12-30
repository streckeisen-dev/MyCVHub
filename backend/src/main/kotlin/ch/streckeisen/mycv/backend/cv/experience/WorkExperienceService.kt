package ch.streckeisen.mycv.backend.cv.experience

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

private const val NOT_FOUND_MESSAGE_KEY = "${MYCV_KEY_PREFIX}.workExperience.notFound"
private const val ACCESSED_DENIED_MESSAGE_KEY = "${MYCV_KEY_PREFIX}.workExperience.accessDenied"

@Service
class WorkExperienceService(
    private val workExperienceRepository: WorkExperienceRepository,
    private val workExperienceValidationService: WorkExperienceValidationService,
    private val profileService: ProfileService
) {
    @Transactional
    fun save(
        applicantId: Long,
        workExperience: WorkExperienceUpdateDto,
        allowFutureStartDate: Boolean = false
    ): Result<WorkExperienceEntity> {
        val existingWorkExperience = if (workExperience.id != null) {
            workExperienceRepository.findById(workExperience.id)
                .getOrElse { return Result.failure(LocalizedException(NOT_FOUND_MESSAGE_KEY)) }
        } else null

        if (existingWorkExperience != null && existingWorkExperience.profile.account.id != applicantId) {
            return Result.failure(LocalizedException(ACCESSED_DENIED_MESSAGE_KEY))
        }

        val profile = if (existingWorkExperience != null) {
            existingWorkExperience.profile
        } else {
            profileService.findByAccountId(applicantId)
                .onFailure { return Result.failure(it) }
                .getOrNull()!!
        }

        workExperienceValidationService.validateWorkExperience(workExperience, allowFutureStartDate)
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
            return Result.failure(LocalizedException(NOT_FOUND_MESSAGE_KEY))
        }

        if (applicantId != workExperience.profile.account.id) {
            return Result.failure(LocalizedException(ACCESSED_DENIED_MESSAGE_KEY))
        }

        workExperienceRepository.delete(workExperience)
        return Result.success(Unit)
    }
}
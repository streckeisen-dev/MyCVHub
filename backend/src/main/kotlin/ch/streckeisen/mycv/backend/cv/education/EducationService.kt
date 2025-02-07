package ch.streckeisen.mycv.backend.cv.education

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
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
                .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.education.notFound")) }
        } else null

        if (existingEducation != null && existingEducation.profile.account.id != accountId) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.education.accessDenied"))
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
                    id = existingEducation?.id,
                    institution = educationUpdate.institution!!,
                    location = educationUpdate.location!!,
                    educationStart = educationUpdate.educationStart!!,
                    educationEnd = educationUpdate.educationEnd,
                    degreeName = educationUpdate.degreeName!!,
                    description = if (educationUpdate.description == "") null else educationUpdate.description,
                    profile = profile
                )
            )
        )
    }

    @Transactional
    fun delete(accountId: Long, id: Long): Result<Unit> {
        val education = educationRepository.findById(id)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.education.notFound")) }

        if (education.profile.account.id != accountId) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.education.accessDenied"))
        }

        educationRepository.delete(education)
        return Result.success(Unit)
    }
}
package ch.streckeisen.mycv.backend.cv.skill

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.EntityNotFoundException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class SkillService(
    private val skillRepository: SkillRepository,
    private val skillValidationService: SkillValidationService,
    private val profileService: ProfileService
) {
    @Transactional
    fun save(accountId: Long, skillUpdate: SkillUpdateDto): Result<SkillEntity> {
        val existingSkill = if (skillUpdate.id != null) {
            skillRepository.findById(skillUpdate.id)
                .getOrElse { return Result.failure(EntityNotFoundException("This skill does not exist")) }
        } else null

        val profile = if (existingSkill != null) {
            existingSkill.profile
        } else {
            profileService.findByAccountId(accountId)
                .getOrElse { return Result.failure(it) }
        }

        if (profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You don't have permission to modify this skill"))
        }

        skillValidationService.validateSkill(skillUpdate)
            .onFailure { return Result.failure(it) }

        return Result.success(
            skillRepository.save(
                SkillEntity(
                    existingSkill?.id,
                    skillUpdate.name!!,
                    skillUpdate.type!!,
                    skillUpdate.level!!,
                    profile
                )
            )
        )
    }

    @Transactional
    fun delete(accountId: Long, skillId: Long): Result<Unit> {
        val skill = skillRepository.findById(skillId)
            .getOrElse { return Result.failure(EntityNotFoundException("This skill does not exist")) }

        if (skill.profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You don't have permission to delete this skill"))
        }

        skillRepository.delete(skill)
        return Result.success(Unit)
    }
}
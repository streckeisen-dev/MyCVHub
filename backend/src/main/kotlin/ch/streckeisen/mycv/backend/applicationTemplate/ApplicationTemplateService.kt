package ch.streckeisen.mycv.backend.applicationTemplate

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import kotlin.jvm.optionals.getOrElse

private const val NOT_FOUND_MESSAGE_KEY = "${MYCV_KEY_PREFIX}.applicationTemplate.notFound"
private const val ACCESS_DENIED_MESSAGE_KEY = "${MYCV_KEY_PREFIX}.applicationTemplate.accessDenied"

@Service
class ApplicationTemplateService(
    private val applicationTemplateRepository: ApplicationTemplateRepository,
    private val profileService: ProfileService,
    private val applicationTemplateValidationService: ApplicationTemplateValidationService,
    private val objectMapper: ObjectMapper
) {
    fun findApplicationTemplates(accountId: Long): List<ApplicationTemplate> {
        return applicationTemplateRepository.findByAccountId(accountId)
            .map { entity -> entity.toFullObject(objectMapper) }
    }

    fun save(accountId: Long, applicationTemplate: ApplicationTemplateUpdateDto): Result<ApplicationTemplate> {
        val existingTemplate =
            if (applicationTemplate.id != null) applicationTemplateRepository.findById(applicationTemplate.id)
                .getOrElse {
                    return Result.failure(
                        LocalizedException(NOT_FOUND_MESSAGE_KEY)
                    )
                } else null

        if (existingTemplate != null && existingTemplate.account.id != accountId)  {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        val profile = existingTemplate?.account?.profile ?: profileService.findByAccountId(accountId)
            .getOrElse { return Result.failure(it) }

        applicationTemplateValidationService.validateUpdate(accountId, applicationTemplate, profile)
            .onFailure { return Result.failure(it) }

        val template = ApplicationTemplateEntity(
            id = existingTemplate?.id,
            name = applicationTemplate.name!!,
            cvConfiguration = objectMapper.writeValueAsString(applicationTemplate.cvConfiguration!!.toCvConfiguration()),
            documentChecklist = if (applicationTemplate.documentChecklist != null) objectMapper.writeValueAsString(applicationTemplate.documentChecklist) else null,
            account = profile.account
        )

        val updated = applicationTemplateRepository.save(template)
        return Result.success(updated.toFullObject(objectMapper))
    }

    fun delete(accountId: Long, applicationTemplateId: Long): Result<Unit> {
        val template = applicationTemplateRepository.findById(applicationTemplateId)
            .getOrElse { return Result.failure(LocalizedException(NOT_FOUND_MESSAGE_KEY)) }

        if (template.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        applicationTemplateRepository.delete(template)
        return Result.success(Unit)
    }
}
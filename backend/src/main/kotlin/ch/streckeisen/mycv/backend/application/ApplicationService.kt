package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.application.dto.ApplicationTransitionRequestDto
import ch.streckeisen.mycv.backend.application.dto.ApplicationUpdateDto
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

private val availableTransitions: Map<ApplicationStatus, List<ApplicationTransition>> =
    ApplicationStatus.entries.associateWith { status ->
        ApplicationTransition.entries.filter { transition ->
            transition.from.contains(status)
        }
    }

private val transitions: Map<Int, ApplicationTransition> =
    ApplicationTransition.entries.associateBy { transition -> transition.id }

private const val ACCESS_DENIED_MESSAGE_KEY = "$MYCV_KEY_PREFIX.application.accessDenied"
private const val APPLICATION_NOT_FOUND_MESSAGE_KEY = "$MYCV_KEY_PREFIX.application.notFound"
private const val TRANSITION_NOT_FOUND_MESSAGE_KEY = "$MYCV_KEY_PREFIX.application.transition.notFound"
private const val TRANSITION_NOT_ALLOWED_MESSAGE_KEY = "$MYCV_KEY_PREFIX.application.transition.notAllowed"
private const val ARCHIVED_MESSAGE_KEY = "$MYCV_KEY_PREFIX.application.archived"
private const val ARCHIVE_OPEN_NOT_ALLOWED_MESSAGE_KEY = "$MYCV_KEY_PREFIX.application.archiveOpenNotAllowed"

private const val DESCENDING_KEY = "descending"

private const val UPDATED_COLUMN_KEY = "updatedAt"
private const val CREATED_COLUMN_KEY = "createdAt"

@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val applicationHistoryRepository: ApplicationHistoryRepository,
    private val applicationValidationService: ApplicationValidationService,
    private val applicationAccountService: ApplicantAccountService
) {
    @Transactional
    fun findById(accountId: Long, applicationId: Long): Result<ApplicationEntity> {
        val application = applicationRepository.findById(applicationId)
            .getOrElse { return Result.failure(LocalizedException(APPLICATION_NOT_FOUND_MESSAGE_KEY)) }

        if (application.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        return Result.success(application)
    }

    @Transactional(readOnly = true)
    fun searchApplications(
        accountId: Long,
        page: Int,
        pageSize: Int,
        searchTerm: String?,
        status: ApplicationStatus?,
        includeArchived: Boolean,
        sort: String?,
        sortDirection: String?
    ): Result<Page<ApplicationEntity>> {
        val pageable = if (!sort.isNullOrBlank()) {
            val sortBy = if (sortDirection == DESCENDING_KEY) {
                Sort.by(Sort.Direction.DESC, sort)
            } else {
                Sort.by(Sort.Direction.ASC, sort)
            }
            PageRequest.of(page, pageSize, sortBy)
        } else PageRequest.of(
            page,
            pageSize,
            Sort.by(
                Sort.Order(Sort.Direction.DESC, UPDATED_COLUMN_KEY),
                Sort.Order(Sort.Direction.DESC, CREATED_COLUMN_KEY)
            )
        )
        val result = applicationRepository.searchByAccountId(accountId, searchTerm, status, includeArchived, pageable)
        return Result.success(result)
    }

    fun getAvailableTransitions(applicationStatus: ApplicationStatus): Result<List<ApplicationTransition>> {
        return Result.success(availableTransitions[applicationStatus] ?: emptyList())
    }

    @Transactional(readOnly = true)
    fun getApplicationStats(accountId: Long): Result<List<ApplicationStat>> {
        val result = applicationRepository.getApplicationStats(accountId)
        return Result.success(result)
    }

    @Transactional
    fun save(accountId: Long, update: ApplicationUpdateDto): Result<ApplicationEntity> {
        applicationValidationService.validateUpdate(update)
            .onFailure { return Result.failure(it) }

        val application = if (update.id != null) {
            applicationRepository.findById(update.id)
                .getOrElse { return Result.failure(LocalizedException(APPLICATION_NOT_FOUND_MESSAGE_KEY)) }
        } else {
            null
        }

        if (application != null && application.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        if (application != null && application.isArchived) {
            return Result.failure(LocalizedException(ARCHIVED_MESSAGE_KEY))
        }

        val account = if (application != null) {
            application.account
        } else {
            applicationAccountService.findById(accountId)
                .onFailure { return Result.failure(it) }
                .getOrNull()!!
        }

        val updatedApplication = ApplicationEntity(
            id = application?.id,
            jobTitle = update.jobTitle!!,
            company = update.company!!,
            status = application?.status ?: ApplicationStatus.UNSENT,
            createdAt = application?.createdAt ?: LocalDateTime.now(),
            updatedAt = if (application != null) LocalDateTime.now() else null,
            source = update.source,
            description = update.description,
            history = application?.history ?: emptyList(),
            account = account
        )

        val saved = applicationRepository.save(updatedApplication)
        return Result.success(saved)
    }

    @Transactional
    fun transition(
        accountId: Long,
        transitionId: Int,
        transitionRequest: ApplicationTransitionRequestDto
    ): Result<ApplicationEntity> {
        val transition = transitions[transitionId]
            ?: return Result.failure(LocalizedException(TRANSITION_NOT_FOUND_MESSAGE_KEY))

        applicationValidationService.validateTransition(transitionRequest)
            .onFailure { return Result.failure(it) }

        val application = applicationRepository.findById(transitionRequest.applicationId!!)
            .getOrElse { return Result.failure(LocalizedException(APPLICATION_NOT_FOUND_MESSAGE_KEY)) }

        if (application.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        if (availableTransitions[application.status]?.none { it.id == transitionId } == true) {
            return Result.failure(LocalizedException(TRANSITION_NOT_ALLOWED_MESSAGE_KEY))
        }

        val historyEntry = applicationHistoryRepository.save(
            ApplicationHistoryEntity(
                id = null,
                source = application.status,
                target = transition.to,
                comment = transitionRequest.comment,
                timestamp = LocalDateTime.now(),
                application = application
            )
        )

        val updatedApplication = ApplicationEntity(
            id = application.id,
            jobTitle = application.jobTitle,
            company = application.company,
            status = transition.to,
            createdAt = application.createdAt,
            updatedAt = LocalDateTime.now(),
            source = application.source,
            description = application.description,
            history = application.history + listOf(historyEntry),
            account = application.account
        )

        val saved = applicationRepository.save(updatedApplication)
        return Result.success(saved)
    }

    @Transactional
    fun archive(accountId: Long, applicationId: Long): Result<Unit> {
        val application = applicationRepository.findById(applicationId)
            .getOrElse { return Result.failure(LocalizedException(APPLICATION_NOT_FOUND_MESSAGE_KEY)) }

        if (application.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        if (application.isArchived) {
            return Result.success(Unit)
        }

        if (availableTransitions[application.status]?.size ?: 0 > 0) {
            return Result.failure(LocalizedException(ARCHIVE_OPEN_NOT_ALLOWED_MESSAGE_KEY))
        }


        val updatedApplication = ApplicationEntity(
            id = application.id,
            jobTitle = application.jobTitle,
            company = application.company,
            status = application.status,
            createdAt = application.createdAt,
            updatedAt = LocalDateTime.now(),
            source = application.source,
            description = application.description,
            history = application.history,
            account = application.account,
            isArchived = true
        )

        applicationRepository.save(updatedApplication)
        return Result.success(Unit)
    }

    @Transactional
    fun delete(accountId: Long, applicationId: Long): Result<Unit> {
        val application = applicationRepository.findById(applicationId)
            .getOrElse { return Result.failure(LocalizedException(APPLICATION_NOT_FOUND_MESSAGE_KEY)) }

        if (application.account.id != accountId) {
            return Result.failure(LocalizedException(ACCESS_DENIED_MESSAGE_KEY))
        }

        applicationRepository.delete(application)
        return Result.success(Unit)
    }
}
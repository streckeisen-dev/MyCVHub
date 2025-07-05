package ch.streckeisen.mycv.backend.cv.project

import ch.streckeisen.mycv.backend.cv.profile.ProfileService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectValidationService: ProjectValidationService,
    private val profileService: ProfileService
) {
    @Transactional
    fun save(accountId: Long, projectUpdate: ProjectUpdateDto): Result<ProjectEntity> {
        projectValidationService.validateProject(projectUpdate)
            .onFailure { return Result.failure(it) }

        val existingProject = projectUpdate.id?.let {
            projectRepository.findById(it)
                .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.project.notFound")) }
        }
        val profile = existingProject?.profile ?: profileService.findByAccountId(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }

        if (accountId != profile.account.id) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.project.accessDenied"))
        }

        val project = ProjectEntity(
            id = existingProject?.id,
            name = projectUpdate.name!!,
            role = projectUpdate.role!!,
            description = projectUpdate.description!!,
            projectStart = projectUpdate.projectStart!!,
            projectEnd = projectUpdate.projectEnd,
            links = projectUpdate.links?.map { ProjectLink(url = it.url!!, displayName = it.displayName!!, type = it.type!!) } ?: emptyList(),
            profile = profile
        )
        return Result.success(projectRepository.save(project))
    }

    @Transactional
    fun delete(accountId: Long, projectId: Long): Result<Unit> {
        val project = projectRepository.findById(projectId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.project.notFound")) }

        if (accountId != project.profile.account.id) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.project.accessDenied"))
        }

        projectRepository.delete(project)
        return Result.success(Unit)
    }
}
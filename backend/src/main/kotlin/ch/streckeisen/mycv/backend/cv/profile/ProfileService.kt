package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val profileValidationService: ProfileValidationService,
    private val applicantAccountService: ApplicantAccountService
) {
    @Transactional(readOnly = true)
    fun findByAlias(alias: String): Result<ProfileEntity> {
        return profileRepository.findByAlias(alias)
            .map { Result.success(it) }
            .orElse(Result.failure(ResultNotFoundException("Profile not found")))
    }

    @Transactional
    fun findByAccountId(accountId: Long): Result<ProfileEntity> {
        return profileRepository.findByAccountId(accountId)
            .map { Result.success(it) }
            .orElse(Result.failure(ResultNotFoundException("Profile not found")))
    }

    @Transactional
    fun updateGeneralInformation(accountId: Long, profileInformationUpdate: GeneralProfileInformationUpdateDto): Result<ProfileEntity> {
        profileValidationService.validateProfileInformation(accountId, profileInformationUpdate)
            .onFailure { return Result.failure(it) }

        val account = applicantAccountService.findById(accountId).getOrNull()
        if (account == null) {
            return Result.failure(IllegalArgumentException("Account is invalid"))
        }

        val existingProfile = findByAccountId(accountId).getOrElse { null }

        val profile = ProfileEntity(
            id = existingProfile?.id,
            alias = profileInformationUpdate.alias!!,
            jobTitle = profileInformationUpdate.jobTitle!!,
            aboutMe = profileInformationUpdate.aboutMe!!,
            isProfilePublic = profileInformationUpdate.isProfilePublic ?: true,
            isEmailPublic = profileInformationUpdate.isEmailPublic ?: false,
            isPhonePublic = profileInformationUpdate.isPhonePublic ?: false,
            isAddressPublic = profileInformationUpdate.isAddressPublic ?: false,
            workExperiences = existingProfile?.workExperiences ?: emptyList(),
            education = existingProfile?.education ?: emptyList(),
            skills = existingProfile?.skills ?: emptyList(),
            account = account
        )

        return Result.success(profileRepository.save(profile))
    }
}
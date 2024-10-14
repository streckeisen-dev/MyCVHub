package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import kotlin.jvm.optionals.getOrElse

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val profileValidationService: ProfileValidationService,
    private val applicantAccountService: ApplicantAccountService,
    private val profilePictureService: ProfilePictureService
) {
    @Transactional(readOnly = true)
    fun findByAlias(accountId: Long?, alias: String): Result<ProfileEntity> {
        val profile = profileRepository.findByAlias(alias)
            .getOrElse { return Result.failure(ResultNotFoundException("Profile not found")) }

        if (!profile.isProfilePublic && profile.account.id != accountId) {
            return Result.failure(AccessDeniedException("You don't have permission to view this profile"))
        }
        return Result.success(profile)
    }

    @Transactional
    fun findByAccountId(accountId: Long): Result<ProfileEntity> {
        return profileRepository.findByAccountId(accountId)
            .map { Result.success(it) }
            .orElse(Result.failure(ResultNotFoundException("Profile not found")))
    }

    @Transactional
    fun save(
        accountId: Long,
        profileInformationUpdate: GeneralProfileInformationUpdateDto,
        profilePictureUpdate: MultipartFile?
    ): Result<ProfileEntity> {
        val account = applicantAccountService.findById(accountId).getOrNull()
        if (account == null) {
            return Result.failure(ResultNotFoundException("Account is invalid"))
        }

        val existingProfile = account.profile
        profileValidationService.validateProfileInformation(
            accountId,
            profileInformationUpdate,
            profilePictureUpdate,
            existingProfile == null
        ).onFailure { return Result.failure(it) }

        val profilePicture = if (profilePictureUpdate != null) {
            profilePictureService.store(accountId, profilePictureUpdate, existingProfile?.profilePicture)
                .getOrElse { return Result.failure(it) }
        } else {
            existingProfile!!.profilePicture
        }

        val profile = ProfileEntity(
            id = existingProfile?.id,
            alias = profileInformationUpdate.alias!!,
            jobTitle = profileInformationUpdate.jobTitle!!,
            bio = profileInformationUpdate.bio,
            isProfilePublic = profileInformationUpdate.isProfilePublic ?: false,
            isEmailPublic = profileInformationUpdate.isEmailPublic ?: false,
            isPhonePublic = profileInformationUpdate.isPhonePublic ?: false,
            isAddressPublic = profileInformationUpdate.isAddressPublic ?: false,
            profilePicture = profilePicture,
            workExperiences = existingProfile?.workExperiences ?: emptyList(),
            education = existingProfile?.education ?: emptyList(),
            skills = existingProfile?.skills ?: emptyList(),
            account = account
        )
        return Result.success(profileRepository.save(profile))
    }
}
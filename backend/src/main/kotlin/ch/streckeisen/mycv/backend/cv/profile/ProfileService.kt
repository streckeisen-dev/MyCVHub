package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
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
    fun findByUsername(accountId: Long?, username: String): Result<ProfileEntity> {
        val profile = profileRepository.findByAccountUsername(username)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }

        if (!profile.isProfilePublic && profile.account.id != accountId) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.accessDenied"))
        }
        return Result.success(profile)
    }

    @Transactional
    fun findByAccountId(accountId: Long): Result<ProfileEntity> {
        return profileRepository.findByAccountId(accountId)
            .map { Result.success(it) }
            .orElse(Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")))
    }

    @Transactional
    fun save(
        accountId: Long,
        profileInformationUpdate: GeneralProfileInformationUpdateDto,
        profilePictureUpdate: MultipartFile?
    ): Result<ProfileEntity> {
        val account = applicantAccountService.findById(accountId).getOrNull()
        if (account == null) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.account.notFound"))
        }

        val existingProfile = account.profile
        profileValidationService.validateProfileInformation(
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

        val isProfilePublic = profileInformationUpdate.isProfilePublic ?: existingProfile?.isProfilePublic ?: false
        val isEmailPublic = profileInformationUpdate.isEmailPublic ?: existingProfile?.isEmailPublic ?: false
        val isPhonePublic = profileInformationUpdate.isPhonePublic ?: existingProfile?.isPhonePublic ?: false
        val isAddressPublic = profileInformationUpdate.isAddressPublic ?: existingProfile?.isAddressPublic ?: false
        val hideDescriptions = profileInformationUpdate.hideDescriptions ?: existingProfile?.hideDescriptions ?: true

        val profile = ProfileEntity(
            id = existingProfile?.id,
            jobTitle = profileInformationUpdate.jobTitle!!,
            bio = if (profileInformationUpdate.bio == "") null else profileInformationUpdate.bio,
            isProfilePublic = isProfilePublic,
            isEmailPublic = isEmailPublic,
            isPhonePublic = isPhonePublic,
            isAddressPublic = isAddressPublic,
            hideDescriptions = hideDescriptions,
            profilePicture = profilePicture,
            workExperiences = existingProfile?.workExperiences ?: emptyList(),
            education = existingProfile?.education ?: emptyList(),
            skills = existingProfile?.skills ?: emptyList(),
            account = account
        )
        return Result.success(profileRepository.save(profile))
    }
}
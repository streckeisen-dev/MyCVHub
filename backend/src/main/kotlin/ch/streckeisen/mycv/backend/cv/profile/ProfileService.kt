package ch.streckeisen.mycv.backend.cv.profile

import ch.streckeisen.mycv.backend.account.ApplicantAccountService
import ch.streckeisen.mycv.backend.cv.generator.CVEducationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVGenerationSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVProjectLinkSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVProjectSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVSkillSnapshot
import ch.streckeisen.mycv.backend.cv.generator.CVWorkExperienceSnapshot
import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureService
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.publicapi.profile.dto.PublicProfileDto
import ch.streckeisen.mycv.backend.publicapi.profile.toPublicDto
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
    fun findByUsername(accountId: Long?, username: String): Result<PublicProfileDto> {
        val profile = profileRepository.findByAccountUsername(username)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }

        if (!profile.isProfilePublic && profile.account.id != accountId) {
            return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.accessDenied"))
        }
        val profilePicture = profilePictureService.get(profile.account.id!!, profile)
            .getOrElse { return Result.failure(it) }

        return Result.success(profile.toPublicDto(profilePicture.uri.toString()))
    }

    @Transactional(readOnly = true)
    fun findByAccountId(accountId: Long): Result<ProfileDto> {
        val profile = profileRepository.findByAccountId(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }

        val profilePicture = profilePictureService.get(accountId, profile)
            .getOrElse { return Result.failure(it) }

        return Result.success(profile.toDto(profilePicture.uri.toString()))
    }

    @Transactional(readOnly = true)
    fun getProfilePictureThumbnail(accountId: Long): Result<ThumbnailDto> {
        val profile = profileRepository.findByAccountId(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }
        val thumbnail = profilePictureService.getThumbnail(accountId, profile)
            .getOrElse { return Result.failure(it) }

        return Result.success(ThumbnailDto(thumbnail.uri.toString()))
    }

    @Transactional(readOnly = true)
    fun findEntityByAccountId(accountId: Long): Result<ProfileEntity> {
        val profile = profileRepository.findByAccountId(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }

        return Result.success(profile)
    }

    @Transactional(readOnly = true)
    fun findByAccountIdForCVGeneration(accountId: Long): Result<CVGenerationSnapshot> {
        val profile = profileRepository.findByAccountId(accountId)
            .getOrElse { return Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")) }

        return Result.success(profile.toCVGenerationSnapshot())
    }

    private fun ProfileEntity.toCVGenerationSnapshot(): CVGenerationSnapshot {
        val details = account.accountDetails
        return CVGenerationSnapshot(
            accountId = account.id!!,
            isVerified = account.isVerified,
            firstName = details?.firstName,
            lastName = details?.lastName,
            email = details?.email,
            phone = details?.phone,
            street = details?.street,
            houseNumber = details?.houseNumber,
            postcode = details?.postcode,
            city = details?.city,
            birthday = details?.birthday,
            language = details?.language,
            profilePicture = profilePicture,
            jobTitle = jobTitle,
            bio = bio,
            workExperiences = workExperiences.map { workExperience ->
                CVWorkExperienceSnapshot(
                    id = workExperience.id,
                    jobTitle = workExperience.jobTitle,
                    company = workExperience.company,
                    positionStart = workExperience.positionStart,
                    positionEnd = workExperience.positionEnd,
                    location = workExperience.location,
                    description = workExperience.description
                )
            },
            education = education.map { education ->
                CVEducationSnapshot(
                    id = education.id,
                    institution = education.institution,
                    location = education.location,
                    educationStart = education.educationStart,
                    educationEnd = education.educationEnd,
                    degreeName = education.degreeName,
                    description = education.description
                )
            },
            projects = projects.map { project ->
                CVProjectSnapshot(
                    id = project.id,
                    name = project.name,
                    role = project.role,
                    description = project.description,
                    projectStart = project.projectStart,
                    projectEnd = project.projectEnd,
                    links = project.links.map { link ->
                        CVProjectLinkSnapshot(
                            url = link.url,
                            displayName = link.displayName,
                            type = link.type
                        )
                    }
                )
            },
            skills = skills.map { skill ->
                CVSkillSnapshot(
                    id = skill.id,
                    name = skill.name,
                    type = skill.type,
                    level = skill.level.toInt()
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun getProfileStats(accountId: Long): Result<ProfileStats> {
        return profileRepository.getProfileStats(accountId)
            .map { Result.success(it) }
            .orElse(Result.failure(LocalizedException("${MYCV_KEY_PREFIX}.profile.notFound")))
    }

    @Transactional
    fun save(
        accountId: Long,
        profileInformationUpdate: GeneralProfileInformationUpdateDto,
        profilePictureUpdate: MultipartFile?
    ): Result<ProfileDto> {
        val account = applicantAccountService.findEntityById(accountId).getOrNull()
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
        val saved = profileRepository.save(profile)
        val profilePictureDto = profilePictureService.get(accountId, saved)
            .getOrElse { return Result.failure(it) }

        return Result.success(saved.toDto(profilePictureDto.uri.toString()))
    }
}

package ch.streckeisen.mycv.backend.privacy

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PrivacySettingsService(
    private val privacySettingsRepository: PrivacySettingsRepository
) {
    @Transactional(readOnly = true)
    fun findByApplicant(applicantId: Long): PrivacySettings? {
        return privacySettingsRepository.findByApplicantId(applicantId)
    }

    fun getDefaultSettings(isProfilePublic: Boolean?): PrivacySettings {
        return PrivacySettings(
            isProfilePublic = isProfilePublic ?: true,
            isEmailPublic = false,
            isPhonePublic = false,
            isBirthdayPublic = false,
            isAddressPublic = false
        )
    }

    @Transactional
    fun update(privacySettingsUpdate: PrivacySettingsUpdateDto, applicantId: Long): Result<PrivacySettings> {
        val validationBuilder = ValidationException.ValidationErrorBuilder()
        val existingPrivacySettings = findByApplicant(applicantId)

        if (existingPrivacySettings == null) {
            validationBuilder.addError("applicantId", "Applicant not found")
        }

        if (validationBuilder.hasErrors()) {
            return Result.failure(validationBuilder.build("Privacy Settings validation failed"))
        }

        val privacySettings = PrivacySettings(
            privacySettingsUpdate.isProfilePublic ?: true,
            privacySettingsUpdate.isEmailPublic ?: false,
            privacySettingsUpdate.isPhonePublic ?: false,
            privacySettingsUpdate.isBirthdayPublic ?: false,
            privacySettingsUpdate.isAddressPublic ?: false,
            existingPrivacySettings!!.applicant,
            existingPrivacySettings.id
        )
        return Result.success(privacySettingsRepository.save(privacySettings))
    }
}
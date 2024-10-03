package ch.streckeisen.mycv.backend.privacy

import org.springframework.data.repository.CrudRepository

interface PrivacySettingsRepository : CrudRepository<PrivacySettings, Long> {
    fun findByApplicantId(applicantId: Long): PrivacySettings
}
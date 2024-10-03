package ch.streckeisen.mycv.backend.privacy

import ch.streckeisen.mycv.backend.cv.applicant.Applicant
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class PrivacySettings(
    val isProfilePublic: Boolean,
    val isEmailPublic: Boolean,
    val isPhonePublic: Boolean,
    val isBirthdayPublic: Boolean,
    val isAddressPublic: Boolean,

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "privacySettings")
    val applicant: Applicant? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
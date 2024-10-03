package ch.streckeisen.mycv.backend.privacy

data class PrivacySettingsUpdateDto(
    val isProfilePublic: Boolean?,
    val isEmailPublic: Boolean?,
    val isPhonePublic: Boolean?,
    val isBirthdayPublic: Boolean?,
    val isAddressPublic: Boolean?
)

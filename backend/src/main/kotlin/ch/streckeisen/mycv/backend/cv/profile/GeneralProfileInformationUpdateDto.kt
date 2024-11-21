package ch.streckeisen.mycv.backend.cv.profile

data class GeneralProfileInformationUpdateDto(
    val jobTitle: String?,
    val bio: String?,
    val isProfilePublic: Boolean?,
    val isEmailPublic: Boolean?,
    val isPhonePublic: Boolean?,
    val isAddressPublic: Boolean?,
    val hideDescriptions: Boolean?
)

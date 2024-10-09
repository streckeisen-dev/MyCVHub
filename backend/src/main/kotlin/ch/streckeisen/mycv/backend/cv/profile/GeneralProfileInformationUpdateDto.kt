package ch.streckeisen.mycv.backend.cv.profile

data class GeneralProfileInformationUpdateDto(
    val alias: String?,
    val jobTitle: String?,
    val aboutMe: String?,
    val isProfilePublic: Boolean?,
    val isEmailPublic: Boolean?,
    val isPhonePublic: Boolean?,
    val isAddressPublic: Boolean?
)

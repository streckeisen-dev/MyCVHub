export interface ProfileUpdateRequestDto {
  profilePicture?: File
  jobTitle?: string
  bio?: string
  isProfilePublic?: boolean
  isEmailPublic?: boolean
  isPhonePublic?: boolean
  isAddressPublic?: boolean
  hideDescriptions?: boolean
}

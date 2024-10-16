export type ProfileUpdateRequestDto = {
  profilePicture?: File
  alias?: string
  jobTitle?: string
  bio?: string
  isProfilePublic?: boolean
  isEmailPublic?: boolean
  isPhonePublic?: boolean
  isAddressPublic?: boolean
}

import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import type { EducationDto } from '@/dto/EducationDto'
import type { SkillDto } from '@/dto/SkillDto'
import type { ProfileThemeDto } from '@/dto/ProfileThemeDto'

export type ProfileDto = {
  jobTitle: string
  bio: string
  isProfilePublic: boolean
  isEmailPublic: boolean
  isPhonePublic: boolean
  isAddressPublic: boolean
  hideDescriptions: boolean
  profilePicture: string
  workExperiences: Array<WorkExperienceDto>
  education: Array<EducationDto>
  skills: Array<SkillDto>,
  theme?: ProfileThemeDto
}

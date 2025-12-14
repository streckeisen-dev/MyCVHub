import { WorkExperienceDto } from '@/types/WorkExperienceDto.ts'
import { EducationDto } from '@/types/EducationDto.ts'
import { SkillDto } from '@/types/SkillDto.ts'
import { ProjectDto } from '@/types/ProjectDto.ts'
import { ProfileThemeDto } from '@/types/ProfileThemeDto.ts'

export interface ProfileDto {
  jobTitle: string
  bio: string
  isProfilePublic: boolean
  isEmailPublic: boolean
  isPhonePublic: boolean
  isAddressPublic: boolean
  hideDescriptions: boolean
  profilePicture: string
  workExperiences: WorkExperienceDto[]
  education: EducationDto[]
  skills: SkillDto[]
  projects: ProjectDto[]
  theme?: ProfileThemeDto
}

import { WorkExperienceDto } from '@/types/profile/workExperience/WorkExperienceDto.ts'
import { EducationDto } from '@/types/profile/education/EducationDto.ts'
import { SkillDto } from '@/types/profile/skill/SkillDto.ts'
import { ProjectDto } from '@/types/profile/project/ProjectDto.ts'
import { ProfileThemeDto } from '@/types/profile/theme/ProfileThemeDto.ts'

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

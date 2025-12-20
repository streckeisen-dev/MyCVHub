import { PublicWorkExperienceDto } from '@/types/profile/workExperience/PublicWorkExperienceDto.ts'
import { PublicSkillDto } from '@/types/profile/skill/PublicSkillDto.ts'
import { PublicEducationDto } from '@/types/profile/education/PublicEducationDto.ts'
import { PublicProjectDto } from '@/types/profile/project/PublicProjectDto.ts'
import { PublicProfileThemeDto } from '@/types/profile/theme/PublicProfileThemeDto.ts'


export interface PublicProfileDto {
  firstName: string
  lastName: string
  jobTitle: string
  bio: string
  email?: string
  phone?: string
  address?: PublicAddressDto
  profilePicture: string
  workExperiences: PublicWorkExperienceDto[]
  skills: PublicSkillDto[]
  education: PublicEducationDto[]
  projects: PublicProjectDto[]
  theme?: PublicProfileThemeDto
  language: string,
}

export interface PublicAddressDto {
  street: string
  houseNumber?: string
  postcode: string
  city: string
  country: string
}

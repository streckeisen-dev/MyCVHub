import type { PublicWorkExperienceDto } from '@/dto/PublicWorkExperienceDto'
import type { PublicSkillDto } from '@/dto/PublicSkillDto'
import type { PublicEducationDto } from '@/dto/PublicEducationDto'
import type { PublicProfileThemeDto } from '@/dto/PublicProfileThemeDto'
import { PublicProjectDto } from '@/dto/PublicProjectDto'

export type PublicProfileDto = {
  firstName: string
  lastName: string
  jobTitle: string
  bio: string
  email?: string
  phone?: string
  address?: PublicAddressDto
  profilePicture: string
  workExperiences: Array<PublicWorkExperienceDto>
  skills: Array<PublicSkillDto>
  education: Array<PublicEducationDto>
  projects: Array<PublicProjectDto>
  theme?: PublicProfileThemeDto
  language: string
}

export type PublicAddressDto = {
  street: string
  houseNumber?: string
  postcode: string
  city: string
  country: string
}

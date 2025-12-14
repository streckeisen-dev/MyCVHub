import { PublicWorkExperienceDto } from '@/types/PublicWorkExperienceDto.ts'
import { PublicSkillDto } from '@/types/PublicSkillDto.ts'
import { PublicEducationDto } from '@/types/PublicEducationDto.ts'
import { PublicProjectDto } from '@/types/PublicProjectDto.ts'
import { PublicProfileThemeDto } from '@/types/PublicProfileThemeDto.ts'


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

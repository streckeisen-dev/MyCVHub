import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'

export interface DashboardInfoDto {
  isVerified: boolean
  profile: ProfileInfoDto
  applications: ApplicationInfoDto[]
}

export interface ProfileInfoDto {
  experienceCount: number
  educationCount: number
  projectCount: number
  skillCount: number
}

export interface ApplicationInfoDto {
  status: ApplicationStatusDto
  count: number
}
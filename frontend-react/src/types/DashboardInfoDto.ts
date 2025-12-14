export interface DashboardInfoDto {
  isVerified: boolean
  profile: ProfileInfoDto
}

export interface ProfileInfoDto {
  experienceCount: number
  educationCount: number
  projectCount: number
  skillCount: number
}
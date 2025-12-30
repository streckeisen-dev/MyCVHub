export interface ApplicationTransitionRequestDto {
  applicationId: number
  comment: string | undefined
  scheduledWorkExperience?: ScheduledWorkExperienceDto
}

export interface ScheduledWorkExperienceDto {
  jobTitle?: string
  company?: string
  positionStart?: string
  location?: string
  description?: string
}

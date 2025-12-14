export interface PublicWorkExperienceDto {
  jobTitle: string
  company: string
  positionStart: string
  positionEnd?: string
  location: string
  description?: string
  workExperiences: PublicWorkExperienceDto[]
}

export type PublicWorkExperienceDto = {
  jobTitle: string
  company: string
  positionStart: string
  positionEnd?: string
  location: string
  description: string
  workExperiences: Array<PublicWorkExperienceDto>
}

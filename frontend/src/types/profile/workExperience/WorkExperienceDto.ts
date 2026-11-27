export interface WorkExperienceDto {
  id: number
  careerBreak?: boolean
  jobTitle: string
  company: string
  positionStart: string
  positionEnd?: string
  location: string
  description: string
}

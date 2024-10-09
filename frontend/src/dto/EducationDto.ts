export type EducationDto = {
  id: number,
  institution: string,
  location: string,
  educationStart: string,
  educationEnd?: string,
  degreeName: string,
  description: string
}
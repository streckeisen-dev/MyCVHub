import { ProjectLink } from '@/dto/ProjectLink'

export type ProjectUpdateDto = {
  id?: number
  name?: string
  role?: string
  description?: string
  projectStart?: string
  projectEnd?: string
  links?: Array<ProjectLink>
}

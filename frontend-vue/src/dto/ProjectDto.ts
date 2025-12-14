import { ProjectLink } from '@/dto/ProjectLink'

export type ProjectDto = {
  id: number
  name: string
  role: string
  description: string
  projectStart: string
  projectEnd?: string
  links: Array<ProjectLink>
}

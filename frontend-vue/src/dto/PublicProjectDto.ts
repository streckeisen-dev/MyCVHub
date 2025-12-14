import { ProjectLink } from '@/dto/ProjectLink'

export type PublicProjectDto = {
  name: string
  role: string
  description: string
  projectStart: string
  projectEnd?: string
  links: Array<ProjectLink>
}

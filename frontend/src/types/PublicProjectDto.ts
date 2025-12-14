import { ProjectLink } from '@/types/ProjectLink.ts'

export interface PublicProjectDto {
  name: string
  role: string
  description: string
  projectStart: string
  projectEnd?: string
  links: ProjectLink[]
}

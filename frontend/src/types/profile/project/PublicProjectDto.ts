import { ProjectLink } from '@/types/profile/project/ProjectLink.ts'

export interface PublicProjectDto {
  name: string
  role: string
  description: string
  projectStart: string
  projectEnd?: string
  links: ProjectLink[]
}

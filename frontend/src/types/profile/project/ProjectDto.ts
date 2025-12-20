import { ProjectLink } from '@/types/profile/project/ProjectLink.ts'

export interface ProjectDto {
  id: number
  name: string
  role: string
  description: string
  projectStart: string
  projectEnd?: string
  links: ProjectLink[]
}

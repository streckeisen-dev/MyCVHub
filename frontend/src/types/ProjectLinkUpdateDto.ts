import { ProjectLinkType } from '@/types/ProjectLink.ts'

export interface ProjectLinkUpdateDto {
  url?: string
  displayName?: string
  type?: ProjectLinkType
}
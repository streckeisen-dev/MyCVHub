import { ProjectLinkType } from '@/types/profile/project/ProjectLink.ts'

export interface ProjectLinkUpdateDto {
  url?: string
  displayName?: string
  type?: ProjectLinkType
}
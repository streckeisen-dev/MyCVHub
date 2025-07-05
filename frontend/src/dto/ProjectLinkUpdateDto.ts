import { ProjectLinkType } from '@/dto/ProjectLink'

export type ProjectLinkUpdateDto = {
  url?: string
  displayName?: string
  type?: ProjectLinkType
}
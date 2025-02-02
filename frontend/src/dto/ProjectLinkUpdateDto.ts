import { ProjectLinkType } from '@/dto/ProjectLink'

export type ProjectLinkUpdateDto = {
  url?: string
  type?: ProjectLinkType
}
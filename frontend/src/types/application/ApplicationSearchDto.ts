import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'

export interface ApplicationSearchDto {
  id: number
  jobTitle: string
  company: string
  status: ApplicationStatusDto
  source: string | undefined
  description: string | undefined
  createdAt: string
  updatedAt: string | undefined
  isArchived: boolean
}

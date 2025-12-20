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
  history: ApplicationHistoryDto[]
}

export interface ApplicationHistoryDto {
  id: number
  from: ApplicationStatusDto
  to: ApplicationStatusDto
  comment: string | undefined
  timestamp: string
}
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'

export interface ApplicationHistoryDto {
  id: number
  from: ApplicationStatusDto
  to: ApplicationStatusDto
  comment: string | undefined
  timestamp: string
}

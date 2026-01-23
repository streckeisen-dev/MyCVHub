import { ApplicationSearchDto } from '@/types/application/ApplicationSearchDto.ts'
import { ApplicationTransitionDto } from '@/types/application/ApplicationTransitionDto.ts'
import { ApplicationHistoryDto } from '@/types/application/ApplicationHistoryDto.ts'

export type ApplicationDetailsDto = ApplicationSearchDto & {
  history: ApplicationHistoryDto[]
  transitions: ApplicationTransitionDto[]
}
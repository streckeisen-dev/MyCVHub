import { ApplicationSearchDto } from '@/types/application/ApplicationSearchDto.ts'
import { ApplicationTransitionDto } from '@/types/application/ApplicationTransitionDto.ts'

export type ApplicationDetailsDto = ApplicationSearchDto & {
  transitions: ApplicationTransitionDto[]
}
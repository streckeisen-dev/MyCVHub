import { CvConfigurationRequestDto } from '@/types/cv/CvConfigurationRequestDto.ts'

export interface ApplicationTemplateUpdateDto {
  id?: number
  name?: string
  cvConfiguration?: CvConfigurationRequestDto
  documentChecklist?: string[]
}
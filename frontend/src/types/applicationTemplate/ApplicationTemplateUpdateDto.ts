import { CvConfigurationRequestDto } from '@/types/cv/CvConfigurationRequestDto.ts'
import { CoverLetterConfigurationUpdateDto } from '@/types/applicationTemplate/CoverLetterConfigurationUpdateDto.ts'

export interface ApplicationTemplateUpdateDto {
  id?: number
  name?: string
  cvConfiguration?: CvConfigurationRequestDto
  coverLetterConfiguration?: CoverLetterConfigurationUpdateDto
}
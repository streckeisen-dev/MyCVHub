import { CvConfigurationDto } from '@/types/applicationTemplate/CvConfigurationDto.ts'
import { CoverLetterConfigurationDto } from '@/types/applicationTemplate/CoverLetterConfigurationDto.ts'

export interface ApplicationTemplateDto {
  id: number
  name: string
  cvConfiguration: CvConfigurationDto
  coverLetterConfiguration: CoverLetterConfigurationDto
}

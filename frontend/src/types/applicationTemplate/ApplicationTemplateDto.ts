import { CvConfigurationDto } from '@/types/applicationTemplate/CvConfigurationDto.ts'

export interface ApplicationTemplateDto {
  id: number
  name: string
  cvConfiguration: CvConfigurationDto
  documentChecklist: string[] | undefined
}

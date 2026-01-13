import { KeyValueObject } from '@/types/KeyValueObject.ts'

export interface CvConfigurationRequestDto {
  includedCvContent: {
    includedWorkExperience?: CvEntrySelectionRequestDto[]
    includedEducation?: CvEntrySelectionRequestDto[]
    includedProjects?: CvEntrySelectionRequestDto[]
    includedSkills?: number[]
  } | undefined

  cvStyle?: string
  cvStyleOptions?: KeyValueObject<string>
}

export interface CvEntrySelectionRequestDto {
  id?: number
  includeDescription?: boolean
}
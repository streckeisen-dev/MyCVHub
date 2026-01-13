import { KeyValueObject } from '@/types/KeyValueObject.ts'

export interface CvConfigurationDto {
  includedCvContent: {
    includedWorkExperience: CvEntrySelectionDto[]
    includedEducation: CvEntrySelectionDto[]
    includedProjects: CvEntrySelectionDto[]
    includedSkills: number[]
  } | undefined
  cvStyle: string
  cvStyleOptions: KeyValueObject<string>
}

export interface CvEntrySelectionDto {
  id: number
  includeDescription: boolean
}
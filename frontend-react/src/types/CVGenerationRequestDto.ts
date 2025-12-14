import { KeyValueObject } from '@/types/KeyValueObject.ts'

export interface CVGenerationRequestDto {
  includedWorkExperience: undefined | IncludedCVItem[]
  includedEducation: undefined | IncludedCVItem[]
  includedProjects: undefined | IncludedCVItem[]
  includedSkills: undefined | IncludedCVItem[]
  templateOptions: undefined | KeyValueObject<string>
}

export interface IncludedCVItem {
  id: number,
  includeDescription: boolean
}

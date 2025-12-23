import { KeyValueObject } from '@/model/KeyValueObject.ts'

export type CVGenerationRequestDto = {
  includedWorkExperience: undefined | Array<IncludedCVItem>
  includedEducation: undefined | Array<IncludedCVItem>
  includedProjects: undefined | Array<IncludedCVItem>
  includedSkills: undefined | Array<IncludedCVItem>
  templateOptions: undefined | KeyValueObject<string>
}

export type IncludedCVItem = {
  id: number,
  includeDescription: boolean
}

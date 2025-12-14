export interface PublicSkillDto {
  name: string
  type: SkillType
  level: number
}

export enum SkillType {
  ProgrammingLanguage = 'PROGRAMMING_LANGUAGE',
  Language = 'LANGUAGE',
  Tool = 'Tool'
}

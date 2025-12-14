import { SkillType } from '@/types/PublicSkillDto.ts'

export interface SkillDto {
  id: number
  name: string
  type: SkillType
  level: number
}

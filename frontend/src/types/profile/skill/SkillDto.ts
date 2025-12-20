import { SkillType } from '@/types/profile/skill/PublicSkillDto.ts'

export interface SkillDto {
  id: number
  name: string
  type: SkillType
  level: number
}

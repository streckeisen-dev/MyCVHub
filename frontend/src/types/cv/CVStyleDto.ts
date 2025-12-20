import { CVStyleOptionType } from '@/types/cv/CVStyleOptionType.ts'

export interface CVStyleDto {
  key: string
  name: string
  description: string
  options: CVStyleOptionDto[]
}

export interface CVStyleOptionDto {
  key: string
  name: string
  type: CVStyleOptionType
  default: string
}

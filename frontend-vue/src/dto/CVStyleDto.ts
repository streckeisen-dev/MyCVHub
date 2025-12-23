import { CVStyleOptionType } from '@/dto/CVStyleOptionType.ts'

export interface CVStyleDto {
  key: string
  name: string
  description: string
  options: Array<CVStyleOptionDto>
}

export interface CVStyleOptionDto {
  key: string
  name: string
  type: CVStyleOptionType
  default: string
}

import { KeyValueObject } from '@/types/KeyValueObject.ts'

export interface ErrorDto {
  message: string
  errors: KeyValueObject<string>
  status: number
}

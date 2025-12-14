import { KeyValueObject } from '@/model/KeyValueObject.ts'

export type ErrorDto = {
  message: string
  errors: KeyValueObject<string>
  status: number
}

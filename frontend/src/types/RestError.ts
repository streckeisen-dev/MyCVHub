import { ErrorDto } from '@/types/ErrorDto.ts'

export class RestError extends Error {
  errorDto?: ErrorDto

  constructor(message: string, errorDto?: ErrorDto) {
    super(message)
    this.errorDto = errorDto
  }
}

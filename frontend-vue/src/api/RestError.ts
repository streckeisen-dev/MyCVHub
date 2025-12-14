import { ErrorDto } from '@/dto/ErrorDto'

export class RestError extends Error {
  errorDto?: ErrorDto

  constructor(message: string, errorDto?: ErrorDto) {
    super(message)
    this.errorDto = errorDto
  }
}

import { AuthLevel } from '@/types/AuthLevel.ts'
import { ThumbnailDto } from '@/types/ThumbnailDto.ts'

export interface AuthResponseDto {
  username: string
  displayName: string
  authLevel: AuthLevel
  language: undefined | string,
  hasProfile: boolean,
  thumbnail: ThumbnailDto | undefined
}
import { AuthLevel } from '@/types/account/AuthLevel.ts'
import { ThumbnailDto } from '@/types/account/ThumbnailDto.ts'

export interface AuthResponseDto {
  username: string
  displayName: string
  authLevel: AuthLevel
  language: undefined | string,
  hasProfile: boolean,
  thumbnail: ThumbnailDto | undefined
}
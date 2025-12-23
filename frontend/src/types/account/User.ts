import { AuthLevel } from '@/types/account/AuthLevel.ts'

export interface User {
  id: number,
  username: string,
  name: string,
  authLevel: AuthLevel
}
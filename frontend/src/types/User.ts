import { AuthLevel } from '@/types/AuthLevel.ts'

export interface User {
  id: number,
  username: string,
  name: string,
  authLevel: AuthLevel
}
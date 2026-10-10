import { AdminRole } from '@/types/admin/AdminRole.ts'

export interface AdminAuthResponseDto {
  username: string
  role: AdminRole
  mustChangePassword: boolean
}

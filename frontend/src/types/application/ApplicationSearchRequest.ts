import { SortDescriptor } from '@heroui/react'

export interface ApplicationSearchRequest {
  page: number
  searchTerm: string | undefined
  status: string | undefined
  includeArchived: boolean | undefined
  sort: SortDescriptor | undefined
  pageSize: string
}

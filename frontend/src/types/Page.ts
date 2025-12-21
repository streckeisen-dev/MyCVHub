export interface Page<T> {
  page: PageInfo,
  content: T[]
}

export interface PageInfo {
  size: number
  number: number
  totalElements: number
  totalPages: number
}
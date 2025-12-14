import {
  compareDatesByYearAndMonth,
  convertStringToDate
} from '@/helpers/DateHelper.ts'

export interface SortByStartAndEndData {
  start: string
  end: string | undefined
}

export function sortByStartAndEndDate(a: SortByStartAndEndData, b: SortByStartAndEndData): number {
  const projectEndA = convertStringToDate(a.end)
  const projectEndB = convertStringToDate(b.end)
  const projectStartA = convertStringToDate(a.start)
  const projectStartB = convertStringToDate(b.start)

  const projectEndComparison = compareDatesByYearAndMonth(projectEndA, projectEndB)
  if (projectEndComparison === 0) {
    return compareDatesByYearAndMonth(projectStartA, projectStartB)
  }
  return projectEndComparison
}
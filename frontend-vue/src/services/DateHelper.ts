import type { UseI18n } from '@/plugins/i18n.ts'

export function toShortDate(dateString: string | undefined, i18n: UseI18n): string {
  const { t, d } = i18n

  if (dateString == null) {
    return t('date.today')
  }
  return d(dateString, 'monthAndYear')
}

export function convertStringToDate(dateString: string | undefined): Date | undefined {
  if (dateString) {
    return new Date(dateString)
  }
  return undefined
}

export function convertDateToString(date: Date | undefined): string | undefined {
  if (date) {
    const month = date.getMonth() < 9 ? `0${date.getMonth() + 1}` : date.getMonth() + 1
    const day = date.getDate() <= 9 ? `0${date.getDate()}` : date.getDate()
    return `${date.getFullYear()}-${month}-${day}`
  }
  return undefined
}


/**
 * Compare two dates by year and month in descending sort order, an undefined date is considered greater than any defined date
 * @param dateA
 * @param dateB
 * @returns 0 if dates are equal, -1 if dateA is greater than dateB, 1 if dateA is less than dateB
 */
export function compareDatesByYearAndMonth(
  dateA: Date | undefined,
  dateB: Date | undefined
): number {
  if (dateA === undefined || dateB === undefined) {
    if (dateA !== undefined && dateB === undefined) {
      return 1
    }

    if (dateA === undefined && dateB !== undefined) {
      return -1
    }
    return 0
  }

  if (dateA?.getFullYear() === dateB?.getFullYear() && dateA?.getMonth() === dateB?.getMonth()) {
    return 0
  }

  if (dateA > dateB) {
    return -1
  } else if (dateA < dateB) {
    return 1
  }
  return 0
}

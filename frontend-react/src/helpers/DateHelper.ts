import {
  fromDate,
  getLocalTimeZone,
  toCalendarDate
} from '@internationalized/date'
import { CalendarDate } from '@heroui/react'
import moment from 'moment/moment'

export function stringToCalendarDate(
  date: string | undefined
): CalendarDate | null {
  if (!date || date === '') {
    return null
  }
  return toCalendarDate(fromDate(new Date(date), getLocalTimeZone()))
}

export function toDateString(
  date: CalendarDate | null
): string | undefined {
  if (!date) {
    return undefined
  }
  return moment(date.toDate(getLocalTimeZone())).format('YYYY-MM-DD')
}

export function formatDate(dateString: string, lang: string): string {
  const date = moment(dateString)
  switch (lang) {
    case 'de':
      return date.format('DD.MM.YYYY')
    default:
      return date.format('MM/DD/YYYY')
  }
}

export function convertStringToDate(dateString: string | undefined): Date | undefined {
  if (dateString) {
    return new Date(dateString)
  }
  return undefined
}

/*export function convertDateToString(date: Date | undefined): string | undefined {
  if (date) {
    const month = date.getMonth() < 9 ? `0${date.getMonth() + 1}` : date.getMonth() + 1
    const day = date.getDate() <= 9 ? `0${date.getDate()}` : date.getDate()
    return `${date.getFullYear()}-${month}-${day}`
  }
  return undefined
}*/


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

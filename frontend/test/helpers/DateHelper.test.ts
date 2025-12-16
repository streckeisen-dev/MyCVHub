import { describe, expect } from 'vitest'
import {
  compareDatesByYearAndMonth,
  convertStringToDate,
  formatDate,
  stringToCalendarDate,
  toDateString
} from '@/helpers/DateHelper.ts'
import { CalendarDate } from '@internationalized/date'

describe('DateHelper tests', () => {
  describe('stringToCalendarDate tests', () => {
    it('stringToCalendarDate should return null for undefined value', () => {
      const result = stringToCalendarDate(undefined)
      expect(result).toBeNull()
    })

    it('stringToCalendarDate should return null for empty string', () => {
      const result = stringToCalendarDate('')
      expect(result).toBeNull()
    })

    it('stringToCalendarDate should return correct date', () => {
      const result = stringToCalendarDate('2022-01-01')
      const expected = new CalendarDate(2022, 0, 1)
      expect(result).toEqual(expected)
    })
  })

  describe('toDateString tests', () => {
    it('toDateString should return correct date string', () => {
      const date = new CalendarDate(2022, 0, 1)
      const result = toDateString(date)
      expect(result).toBe('2022-01-01')
    })

    it('toDateString should return undefined for null value', () => {
      const result = toDateString(null)
      expect(result).toBeUndefined()
    })
  })

  describe('formatDate tests', () => {
    it('formatDate should format german date correctly', () => {
      const result = formatDate('2020-1-1', 'de')
      expect(result).toBe('01.01.2020')
    })

    it('formatDate should format date in english format', () => {
      const result = formatDate('2020-2-1', 'jp')
      expect(result).toBe('02/01/2020')
    })
  })

  describe('convertStringToDate tests', () => {
    it('convertStringToDate should convert string to date correctly', () => {
      const result = convertStringToDate('2020-2-1')
      const expected = new Date(2020, 1, 1) // monthIndex starts at 0
      expect(result).toEqual(expected)
    })

    it('convertStringToDate should return undefined for undefined value', () => {
      const result = convertStringToDate(undefined)
      expect(result).toBeUndefined()
    })
  })

  describe('compareDatesByYearAndMonth tests', () => {
    it('should return 0 for two undefined dates', () => {
      const result = compareDatesByYearAndMonth(undefined, undefined)

      expect(result).toBe(0)
    })

    it('should return 0 for two equal dates', () => {
      const result = compareDatesByYearAndMonth(new Date(2024, 9, 10), new Date(2024, 9, 10))

      expect(result).toBe(0)
    })

    it('should return 0 for two dates in the same month and year', () => {
      const result = compareDatesByYearAndMonth(new Date(2024, 9, 10), new Date(2024, 9, 20))

      expect(result).toBe(0)
    })

    it('should return -1 for 1st undefined and 2nd defined', () => {
      const result = compareDatesByYearAndMonth(undefined, new Date(2024, 9, 10))

      expect(result).toBe(-1)
    })

    it('should return -1 for 1st greater than 2nd', () => {
      const result = compareDatesByYearAndMonth(new Date(2024, 9, 10), new Date(2024, 8, 5))

      expect(result).toBe(-1)
    })

    it('should return 1 for 1st defined and 2nd undefined', () => {
      const result = compareDatesByYearAndMonth(new Date(2024, 9, 10), undefined)

      expect(result).toBe(1)
    })

    it('should return 1 for 1st less than 2nd', () => {
      const result = compareDatesByYearAndMonth(new Date(2024, 8, 5), new Date(2024, 9, 10))

      expect(result).toBe(1)
    })
  })
})

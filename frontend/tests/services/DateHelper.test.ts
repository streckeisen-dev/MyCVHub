import { describe, it, expect } from 'vitest'
import {
  compareDatesByYearAndMonth,
  convertDateToString,
  convertStringToDate,
  toShortDate
} from '../../src/services/DateHelper'
import mockI18n from '../mock/mockI18n'

describe('toShortDate', () => {
  it('should return today for undefined date', () => {
    const result = toShortDate(undefined, mockI18n.global)

    expect(result).toBe('Today')
  })

  it('should return formatted date', () => {
    const result = toShortDate('2021-12-31', mockI18n.global)

    expect(result).toBe('Dec 2021')
  })
})

describe('convertStringToDate', () => {
  it('should return undefined for undefined date', () => {
    const result = convertStringToDate(undefined)

    expect(result).toBe(undefined)
  })

  it('should convert "2024-09-01" to date', () => {
    const result = convertStringToDate('2024-09-01')

    expect(result).not.undefined.null
    expect(result).toBeInstanceOf(Date)
    expect(result.getFullYear()).toBe(2024)
    expect(result.getMonth()).toBe(8)
    expect(result.getDate()).toBe(1)
  })

  it('should fail to convert "20-5-45382" to date', () => {
    try {
      convertStringToDate('20-5-45382')
      expect.fail('Should have thrown an error')
    } catch (error) {
      expect(error).not.undefined
    }
  })
})

describe('convertDateToString', () => {
  it('should return undefined for undefined date', () => {
    const result = convertDateToString(undefined)

    expect(result).toBe(undefined)
  })

  it('should convert "2024-09-01" date to string', () => {
    const result = convertDateToString(new Date(2024, 8, 1))

    expect(result).toBe('2024-09-01')
  })

  it('should convert "2024-10-09" date to string', () => {
    const result = convertDateToString(new Date(2024, 9, 9))

    expect(result).toBe('2024-10-09')
  })

  it('should convert "2024-10-10" date to string', () => {
    const result = convertDateToString(new Date(2024, 9, 10))

    expect(result).toBe('2024-10-10')
  })
})

describe('compareDatesByYearAndMonth', () => {
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

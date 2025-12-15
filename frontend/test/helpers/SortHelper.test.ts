import { describe } from 'vitest'
import { SortByStartAndEndData, sortByStartAndEndDate } from '../../src/helpers/SortHelper'

describe('SortHelper tests', () => {
  it('sortByStartAndEndDate should sort end before start', ()=> {
    const a: SortByStartAndEndData = {
      start: '2020-5-10',
      end: '2020-8-11'
    }
    const b: SortByStartAndEndData = {
      start: '2020-5-11',
      end: '2020-7-10'
    }

    const result = sortByStartAndEndDate(a, b)
    expect(result).toBe(-1)
  })

  it('sortByStartAndEndDate should sort by start if ends are equal', () => {
    const a: SortByStartAndEndData = {
      start: '2020-1-10',
      end: '2020-5-10'
    }
    const b: SortByStartAndEndData = {
      start: '2020-2-11',
      end: '2020-5-10'
    }

    const result = sortByStartAndEndDate(a, b)
    expect(result).toBe(1)
  })
})
import { describe, expect, test } from 'vitest'
import { formatAdminDateTime } from './AdminDateTime.ts'

describe('formatAdminDateTime', () => {
  test('uses fixed UTC+1 in both summer and winter', () => {
    expect(formatAdminDateTime('2026-07-01T12:00:00Z', 'en-GB')).toContain('13:00:00')
    expect(formatAdminDateTime('2026-01-01T12:00:00Z', 'en-GB')).toContain('13:00:00')
  })
})

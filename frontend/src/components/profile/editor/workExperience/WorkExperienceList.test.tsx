import { WorkExperienceList } from '@/components/profile/editor/workExperience/WorkExperienceList.tsx'
import { renderToStaticMarkup } from 'react-dom/server'
import { expect, test, vi } from 'vitest'

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key })
}))

test('shows career breaks in work-history order with dates and a label', () => {
  const careerBreak = {
    careerBreak: true,
    jobTitle: 'Travel sabbatical',
    location: 'Hidden location',
    company: 'Hidden company',
    positionStart: '2023-07-01',
    positionEnd: '2024-01-01',
    description: 'Travelled by train.'
  }
  const job = {
    jobTitle: 'Lead engineer',
    location: 'Zurich',
    company: 'Example company',
    positionStart: '2024-02-01'
  }

  const html = renderToStaticMarkup(<WorkExperienceList workExperiences={[careerBreak, job]} />)

  expect(html).toContain('workExperience.careerBreak')
  expect(html).toContain('Travel sabbatical')
  expect(html).toContain('Jul 2023 - Jan 2024')
  expect(html).toContain('Travelled by train.')
  expect(html).toContain('Example company')
  expect(html).toContain('Zurich')
  expect(html.indexOf('Lead engineer')).toBeLessThan(html.indexOf('Travel sabbatical'))
  expect(html).not.toContain('Hidden location')
  expect(html).not.toContain('Hidden company')
})

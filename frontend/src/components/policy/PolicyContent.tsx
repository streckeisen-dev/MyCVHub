import { ReactNode } from 'react'
import { centerSection, title } from '@/styles/primitives.ts'
import { formatDate } from '@/helpers/DateHelper.ts'
import { useTranslation } from 'react-i18next'
import { PolicySection, PolicySectionProps } from '@/components/policy/PolicySection.tsx'

export type PolicyContentProps = Readonly<{
  title: string
  lastUpdated: string
  content: PolicySectionProps[]
  note: string
}>

export function PolicyContent(props: PolicyContentProps): ReactNode {
  const { t } = useTranslation()
  const { lastUpdated, content, note, title: pageTitle } = props

  return (
    <div className={centerSection()}>
      <h1 className={title()}>{pageTitle}</h1>
      <p>
        {t('legal.lastUpdated')}: {formatDate(lastUpdated)}
      </p>

      <div className="max-w-5xl flex flex-col gap-2">
        {content.map((section, index) => (
          <PolicySection
            key={section.key}
            title={index + 1 + '. ' + section.title}
            content={section.content}
          />
        ))}

        <div>
          <p className="font-bold">{t('legal.note')}:</p>
          <p>{note}</p>
        </div>
      </div>
    </div>
  )
}

import { ReactNode } from 'react'
import { centerSection, h4, title } from '@/styles/primitives.ts'
import { formatDate } from '@/helpers/DateHelper.ts'
import { useTranslation } from 'react-i18next'

export type PolicySectionProps = Readonly<{
  key: string
  title: string
  content: ReactNode
}>

function PolicySection(props: PolicySectionProps): ReactNode {
  const { key, title, content } = props
  return (
    <section key={key} className="flex flex-col gap-2">
      <h4 className={h4()}>{title}</h4>
      <div>{content}</div>
    </section>
  )
}

export type PolicyContentProps = Readonly<{
  lastUpdated: string
  content: PolicySectionProps[]
  note: string
}>

export function PolicyContent(props: PolicyContentProps): ReactNode {
  const { t } = useTranslation()
  const { lastUpdated, content, note } = props

  return (
    <div className={centerSection()}>
      <h1 className={title()}>{t('privacy.title')}</h1>
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

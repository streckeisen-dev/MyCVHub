import React from 'react'
import { useTranslation } from 'react-i18next'
import { centerSection, subtitle, title } from '@/styles/primitives.ts'

export function NotFoundPage(): React.ReactNode {
  const { t } = useTranslation()

  return (
    <section className={centerSection()}>
      <div className="inline-block max-w-lg text-center justify-center">
        <h1 className={title()}>{t('notFound.headline')}</h1>
        <h2 className={subtitle()}>{t('notFound.title')}</h2>
        <p>{t('notFound.text')}</p>
      </div>
    </section>
  )
}

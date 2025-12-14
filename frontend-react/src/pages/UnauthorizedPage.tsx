import { ReactNode } from 'react'
import { centerSection } from '@/styles/primitives.ts'
import { Empty } from '@/components/Empty.tsx'
import { useTranslation } from 'react-i18next'

export function UnauthorizedPage(): ReactNode {
  const {t} = useTranslation()
  return <section className={centerSection()}>
    <Empty headline={t('unauthorized.headline')} title={t('unauthorized.title')} />
  </section>
}

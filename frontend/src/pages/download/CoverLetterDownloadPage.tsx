import { ReactNode } from 'react'
import { CoverLetterDownload } from '@/components/download/coverletter/CoverLetterDownload.tsx'
import { centerSection, title } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'

export function CoverLetterDownloadPage(): ReactNode {
  const { t } = useTranslation()

  return (
    <section className={centerSection()}>
      <h1 className={title()}>{t('coverLetter.download.title')}</h1>
      <p className="md:max-w-1/4 text-center">{t('coverLetter.download.description')}</p>
      <CoverLetterDownload />
    </section>
  )
}

import { useTranslation } from 'react-i18next'
import { centerSection, title } from '@/styles/primitives.ts'
import { CvDownload } from '@/components/download/cv/CvDownload.tsx'

export function CvDownloadPage() {
  const { t } = useTranslation()

  return (
    <section className={centerSection()}>
      <h1 className={title()}>{t('cv.title')}</h1>
      <p className="md:max-w-1/4 text-center">{t('cv.description')}</p>

      <CvDownload />
    </section>
  )
}

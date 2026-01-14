import { useTranslation } from 'react-i18next'
import { centerSection, h3, h4 } from '@/styles/primitives.ts'
import { CvDownload } from '@/components/download/cv/CvDownload.tsx'

export function CvDownloadPage() {
  const { t } = useTranslation()

  return (
    <section className={centerSection()}>
      <h3 className={h3()}>{t('cv.generate')}</h3>
      <p>{t('cv.intro')}</p>
      <h4 className={h4()}>{t('cv.style')}</h4>
      <p>{t('cv.styleExplanation')}</p>

      <CvDownload />
    </section>
  )
}

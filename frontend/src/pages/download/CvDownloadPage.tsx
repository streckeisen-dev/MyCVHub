import { useTranslation } from 'react-i18next'
import { CvDownload } from '@/components/download/cv/CvDownload.tsx'
import { Page, PageHeader, PageIntro, PageTitle } from '@/components/ui/Layout.tsx'

export function CvDownloadPage() {
  const { t } = useTranslation()

  return (
    <Page size="wide">
      <PageHeader align="center">
        <PageTitle>{t('cv.title')}</PageTitle>
        <PageIntro>{t('cv.description')}</PageIntro>
      </PageHeader>

      <CvDownload />
    </Page>
  )
}

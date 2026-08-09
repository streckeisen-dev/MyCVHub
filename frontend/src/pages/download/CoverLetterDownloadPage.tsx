import { ReactNode } from 'react'
import { CoverLetterDownload } from '@/components/download/coverletter/CoverLetterDownload.tsx'
import { useTranslation } from 'react-i18next'
import { Page, PageHeader, PageIntro, PageTitle } from '@/components/ui/Layout.tsx'

export function CoverLetterDownloadPage(): ReactNode {
  const { t } = useTranslation()

  return (
    <Page size="wide">
      <PageHeader align="center">
        <PageTitle>{t('coverLetter.download.title')}</PageTitle>
        <PageIntro>{t('coverLetter.download.description')}</PageIntro>
      </PageHeader>
      <CoverLetterDownload />
    </Page>
  )
}

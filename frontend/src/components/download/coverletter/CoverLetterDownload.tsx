import { ReactNode, useEffect, useState } from 'react'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import CoverLetterApi from '@/api/CoverLetterApi.ts'
import { useTranslation } from 'react-i18next'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { Empty } from '@/components/Empty.tsx'
import {
  CoverLetterConfigurationData,
  CoverLetterConfigurationEditor
} from '@/components/download/coverletter/CoverLetterConfigurationEditor.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { centerSection } from '@/styles/primitives.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { Button } from '@heroui/react'
import { CoverLetterGenerationRequestDto } from '@/types/coverletter/CoverLetterGenerationRequestDto.ts'
import { openPdfInNewTab } from '@/helpers/DocumentHelper.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'

export function CoverLetterDownload(): ReactNode {
  const { t, i18n } = useTranslation()

  const [isLoading, setIsLoading] = useState(true)
  const [coverLetterStyles, setCoverLetterStyles] = useState<CoverLetterStyleDto[]>()
  const [profile, setProfile] = useState<ProfileDto>()
  const [coverLetterConfig, setCoverLetterConfig] = useState<CoverLetterConfigurationData>({
    style: undefined,
    language: i18n.language,
    mirrorProfileImage: false,
    jobTitle: '',
    company: '',
    contactPerson: {
      firstName: '',
      lastName: ''
    },
    addressee: '',
    salutation: '',
    companyStreet: '',
    companyPostcode: '',
    companyCity: '',
    coverLetterContent: '',
    closing: '',
    documents: []
  })
  const [isDownloading, setIsDownloading] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  useEffect(() => {
    async function loadData() {
      try {
        const styles = await CoverLetterApi.getStyles(i18n.language)
        setCoverLetterStyles(styles)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('coverLetter.stylesError'), error?.message ?? t('error.genericMessage'))
      }

      try {
        const result = await ProfileApi.getProfile(i18n.language)
        setProfile(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      } finally {
        setIsLoading(false)
      }
    }
    loadData()
  }, [])

  function handleCoverLetterConfigChange(config: CoverLetterConfigurationData) {
    setCoverLetterConfig(config)
  }

  async function handleDownload() {
    setIsDownloading(true)

    const request: CoverLetterGenerationRequestDto = {
      language: coverLetterConfig.language,
      mirrorProfileImage: coverLetterConfig.mirrorProfileImage,
      style: coverLetterConfig.style,
      application: {
        jobTitle: coverLetterConfig.jobTitle,
        contactPerson: coverLetterConfig.contactPerson
          ? {
              firstName: coverLetterConfig.contactPerson.firstName,
              lastName: coverLetterConfig.contactPerson.lastName
            }
          : undefined,
        addressee: coverLetterConfig.addressee === '' ? undefined : coverLetterConfig.addressee,
        salutation: coverLetterConfig.salutation,
        company: coverLetterConfig.company,
        companyAddress: {
          street: coverLetterConfig.companyStreet,
          postcode: coverLetterConfig.companyPostcode,
          city: coverLetterConfig.companyCity
        },
        content: coverLetterConfig.coverLetterContent,
        closing: coverLetterConfig.closing
      },
      documents:
        coverLetterConfig.documents.length > 0
          ? coverLetterConfig.documents.map((doc) => doc.name)
          : undefined
    }
    try {
      const data = await CoverLetterApi.getCoverLetter(request, i18n.language)
      openPdfInNewTab(data)
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('coverLetter.downloadFailed'), setErrorMessages, t)
    } finally {
      setIsDownloading(false)
    }
  }

  return (
    <LoadingWrapper isLoading={isLoading} className={`${centerSection()} w-full`}>
      {profile && coverLetterStyles ? (
        <div className="flex flex-col w-full gap-6 items-center justify-center">
          <CoverLetterConfigurationEditor
            styles={coverLetterStyles}
            config={coverLetterConfig}
            onChange={handleCoverLetterConfigChange}
            errorMessages={errorMessages}
          />

          <Button
            color="primary"
            onPress={handleDownload}
            isDisabled={coverLetterConfig.style == null || isDownloading}
            isLoading={isDownloading}
          >
            {t('downloads.action')}
          </Button>
        </div>
      ) : (
        <Empty headline={t('coverLetter.stylesError')} />
      )}
    </LoadingWrapper>
  )
}

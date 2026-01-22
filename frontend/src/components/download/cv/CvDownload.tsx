import {
  CvConfigurationData,
  CvConfigurationEditor
} from '@/components/download/cv/CvConfigurationEditor.tsx'
import { Button } from '@heroui/react'
import { useEffect, useState } from 'react'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import CvApi from '@/api/CvApi.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { CvConfigurationRequestDto } from '@/types/cv/CvConfigurationRequestDto.ts'
import { useTranslation } from 'react-i18next'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { Empty } from '@/components/Empty.tsx'
import { openPdfInNewTab } from '@/helpers/DocumentHelper.ts'

export function CvDownload() {
  const { t, i18n } = useTranslation()

  const [isLoading, setIsLoading] = useState(true)
  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()
  const [profile, setProfile] = useState<ProfileDto>()
  const [templates, setTemplates] = useState<ApplicationTemplateDto[]>()
  const [cvConfig, setCvConfig] = useState<CvConfigurationData>({
    cvStyle: '',
    cvContent: undefined,
    cvStyleOptions: undefined
  })
  const [isGenerating, setIsGenerating] = useState<boolean>(false)

  useEffect(() => {
    async function loadData() {
      try {
        const result = await CvApi.getCVStyles(i18n.language)
        setCvStyles(result)
      } finally {
        setIsLoading(false)
      }

      try {
        const result = await ProfileApi.getProfile(i18n.language)
        setProfile(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      }

      try {
        const result = await ApplicationTemplateApi.getApplicationTemplates(i18n.language)
        setTemplates(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(
          t('applicationTemplate.loadingError'),
          error?.message ?? t('error.genericMesssage')
        )
      } finally {
        setIsLoading(false)
      }
    }

    loadData()
  }, [])

  async function handleDownload() {
    const hasCvItems =
      (cvConfig.cvContent &&
        (cvConfig.cvContent.projects.length > 0 ||
          cvConfig.cvContent.workExperience.length > 0 ||
          cvConfig.cvContent.education.length > 0 ||
          cvConfig.cvContent.skills.length > 0)) ??
      true
    if (!hasCvItems) {
      addErrorToast(t('cv.noItemsSelected'))
      return
    }

    setIsGenerating(true)
    const request: CvConfigurationRequestDto = {
      includedCvContent: cvConfig.cvContent && {
        includedWorkExperience: cvConfig.cvContent.workExperience,
        includedEducation: cvConfig.cvContent.education,
        includedProjects: cvConfig.cvContent.projects,
        includedSkills: cvConfig.cvContent.skills
      },
      cvStyle: cvConfig.cvStyle,
      cvStyleOptions: cvConfig.cvStyleOptions
    }
    try {
      const data = await CvApi.getCV(request, i18n.language)
      openPdfInNewTab(data)
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(t('cv.generateError'), error?.message ?? t('error.genericMessage'))
    } finally {
      setIsGenerating(false)
    }
  }

  function handleCvConfigChange(newConfig: CvConfigurationData) {
    setCvConfig(newConfig)
  }

  return (
    <LoadingWrapper isLoading={isLoading}>
      {profile && cvStyles && templates ? (
        <>
          <CvConfigurationEditor
            config={cvConfig}
            profile={profile}
            cvStyles={cvStyles}
            templates={templates}
            onChange={handleCvConfigChange}
          />

          <Button
            color="primary"
            onPress={handleDownload}
            isLoading={isGenerating}
            isDisabled={cvConfig.cvStyle == null}
          >
            {t('downloads.action')}
          </Button>
        </>
      ) : (
        <Empty headline={t('cv.stylesError')} />
      )}
    </LoadingWrapper>
  )
}

import { useTranslation } from 'react-i18next'
import { centerSection, h3, h4 } from '@/styles/primitives.ts'
import { useEffect, useState } from 'react'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import { Button } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import CvApi from '@/api/CvApi.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { SelectedCvContent } from '@/components/cv/CvContentTreeRoot.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import {
  CvConfigurationData,
  CvConfigurationEditor
} from '@/components/cv/CvConfigurationEditor.tsx'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import {
  CvConfigurationRequestDto,
  CvEntrySelectionRequestDto
} from '@/types/cv/CvConfigurationRequestDto.ts'

function convertToSelectionRequest(selection: SelectedCvContent): CvEntrySelectionRequestDto {
  return { entityId: selection.id, includedDescription: selection.includeDescription }
}

export function CvDownloadPage() {
  const { t, i18n } = useTranslation()

  const [isLoading, setIsLoading] = useState(true)
  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()
  const [profile, setProfile] = useState<ProfileDto>()
  const [cvConfig, setCvConfig] = useState<CvConfigurationData>({
    cvStyle: '',
    cvContent: undefined,
    cvStyleOptions: undefined
  })
  const [isGenerating, setIsGenerating] = useState<boolean>(false)

  useEffect(() => {
    async function loadCvStyles() {
      try {
        const result = await CvApi.getCVStyles(i18n.language)
        setCvStyles(result)
      } finally {
        setIsLoading(false)
      }
    }

    async function loadProfile() {
      try {
        const result = await ProfileApi.getProfile(i18n.language)
        setProfile(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      }
    }

    loadCvStyles()
    loadProfile()
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
        includedWorkExperience: cvConfig.cvContent.workExperience.map(convertToSelectionRequest),
        includedEducation: cvConfig.cvContent.education.map(convertToSelectionRequest),
        includedProjects: cvConfig.cvContent.projects.map(convertToSelectionRequest),
        includedSkills: cvConfig.cvContent.skills
      },
      cvStyle: cvConfig.cvStyle,
      cvStyleOptions: cvConfig.cvStyleOptions
    }
    try {
      const data = await CvApi.getCV(request, i18n.language)
      const fileURL = globalThis.URL.createObjectURL(data)
      const a = document.createElement('a')
      a.href = fileURL
      a.download = 'cv.pdf'
      document.body.appendChild(a)
      a.click()
      a.remove()
      globalThis.URL.revokeObjectURL(fileURL)
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
      {cvStyles && profile ? (
        <section className={centerSection()}>
          <h3 className={h3()}>{t('cv.generate')}</h3>
          <p>{t('cv.intro')}</p>
          <h4 className={h4()}>{t('cv.style')}</h4>
          <p>{t('cv.styleExplanation')}</p>

          <CvConfigurationEditor
            config={cvConfig}
            profile={profile}
            cvStyles={cvStyles}
            onChange={handleCvConfigChange}
          />

          <Button
            color="primary"
            onPress={handleDownload}
            isLoading={isGenerating}
            isDisabled={cvConfig.cvStyle == null}
          >
            {t('cv.download')}
          </Button>
        </section>
      ) : (
        <Empty headline={t('cv.styleError')} />
      )}
    </LoadingWrapper>
  )
}

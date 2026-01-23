import { ReactNode, useEffect, useState } from 'react'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { ApplicationTemplateEditor } from '@/components/applicationTemplate/ApplicationTemplateEditor.tsx'
import { Empty } from '@/components/Empty.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { useTranslation } from 'react-i18next'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import CvApi from '@/api/CvApi.ts'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import CoverLetterApi from '@/api/CoverLetterApi.ts'

export function AddApplicationTemplatePage(): ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [profile, setProfile] = useState<ProfileDto>()
  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()
  const [coverLetterStyles, setCoverLetterStyles] = useState<CoverLetterStyleDto[]>()

  useEffect(() => {
    async function loadData() {
      try {
        const profile = await ProfileApi.getProfile(i18n.language)
        setProfile(profile)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      }

      try {
        const styles = await CoverLetterApi.getStyles(i18n.language)
        setCoverLetterStyles(styles)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('coverLetter.stylesError'), error?.message ?? t('error.genericMessage'))
      }

      try {
        const styles = await CvApi.getCVStyles(i18n.language)
        setCvStyles(styles)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('cv.styleError'), error?.message ?? t('error.genericMessage'))
      } finally {
        setIsLoading(false)
      }
    }
    loadData()
  }, [])

  function handleSave(template: ApplicationTemplateDto) {
    navigate(getRoutePath(RouteId.ApplicationTemplateDetails, undefined, template.id.toString()))
  }

  function handleCancel() {
    navigate(getRoutePath(RouteId.ApplicationTemplateOverview))
  }

  return (
    <LoadingWrapper isLoading={isLoading}>
      {profile == null || cvStyles == null || coverLetterStyles == null ? (
        <Empty headline={'No profile'} />
      ) : (
        <ApplicationTemplateEditor
          onSave={handleSave}
          onCancel={handleCancel}
          profile={profile}
          cvStyles={cvStyles}
          coverLetterStyles={coverLetterStyles}
        />
      )}
    </LoadingWrapper>
  )
}

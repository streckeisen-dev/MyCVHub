import { ReactNode, useEffect, useState } from 'react'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams } from 'react-router-dom'
import { ApplicationTemplateEditor } from '@/components/applicationTemplate/ApplicationTemplateEditor.tsx'
import { Empty } from '@/components/Empty.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import CvApi from '@/api/CvApi.ts'

export function EditApplicationTemplatePage(): ReactNode {
  const { t, i18n } = useTranslation()
  const params = useParams()
  const navigate = useNavigate()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [template, setTemplate] = useState<ApplicationTemplateDto>()
  const [profile, setProfile] = useState<ProfileDto>()
  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()

  useEffect(() => {
    async function loadData() {
      try {
        const styles = await CvApi.getCVStyles(i18n.language)
        setCvStyles(styles)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('cv.styleError'), error?.message ?? t('error.genericMessage'))
      }

      try {
        const profile = await ProfileApi.getProfile(i18n.language)
        setProfile(profile)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      }

      const templateId = params.id ? Number.parseInt(params.id) : undefined
      if (templateId) {
        try {
          const template = await ApplicationTemplateApi.getApplicationTemplate(
            templateId,
            i18n.language
          )
          setTemplate(template)
        } catch (e) {
          const error = (e as RestError).errorDto
          addErrorToast(
            t('applicationTemplate.loadingError'),
            error?.message ?? t('error.genericMessage')
          )
        } finally {
          setIsLoading(false)
        }
      } else {
        addErrorToast(t('applicationTemplate.loadingError'))
        setIsLoading(false)
      }
    }
    loadData()
  }, [])

  function handleSave(template: ApplicationTemplateDto) {
    navigate(getRoutePath(RouteId.ApplicationTemplateDetails, undefined, template.id.toString()))
  }

  function handleCancel() {
    if (params.id) {
      navigate(getRoutePath(RouteId.ApplicationTemplateDetails, undefined, params.id))
    }
  }

  return (
    <LoadingWrapper isLoading={isLoading}>
      {profile && template && cvStyles ? (
        <ApplicationTemplateEditor
          onSave={handleSave}
          onCancel={handleCancel}
          profile={profile}
          initialValue={template}
          cvStyles={cvStyles}
        />
      ) : (
        <Empty headline={'Error'} />
      )}
    </LoadingWrapper>
  )
}

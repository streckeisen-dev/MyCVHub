import { ReactNode, useEffect, useState } from 'react'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { useTranslation } from 'react-i18next'
import { RestError } from '@/types/RestError.ts'
import { Empty } from '@/components/Empty.tsx'
import { Button } from '@heroui/react'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { FaArrowLeft, FaPen, FaTrash } from 'react-icons/fa6'
import { h1, h4 } from '@/styles/primitives.ts'
import { DeleteApplicationTemplateModal } from '@/components/applicationTemplate/DeleteApplicationTemplateModal.tsx'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import {
  CvConfigurationDto,
  CvEntrySelectionDto
} from '@/types/applicationTemplate/CvConfigurationDto.ts'
import {
  CvConfigurationData,
  CvConfigurationEditor
} from '@/components/cv/CvConfigurationEditor.tsx'
import { SelectedCvContent } from '@/components/cv/CvContentTreeRoot.tsx'
import CvApi from '@/api/CvApi.ts'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'

function toSelectedContent(selection: CvEntrySelectionDto): SelectedCvContent {
  return {
    id: selection.id,
    includeDescription: selection.includeDescription
  }
}

function toConfigData(cvConfig: CvConfigurationDto): CvConfigurationData {
  return {
    cvContent: cvConfig.includedCvContent
      ? {
          workExperience: cvConfig.includedCvContent.includedWorkExperience.map(toSelectedContent),
          education: cvConfig.includedCvContent.includedEducation.map(toSelectedContent),
          projects: cvConfig.includedCvContent.includedProjects.map(toSelectedContent),
          skills: cvConfig.includedCvContent.includedSkills
        }
      : undefined,
    cvStyle: cvConfig.cvStyle,
    cvStyleOptions: cvConfig.cvStyleOptions
  }
}

export function ApplicationTemplateDetailsPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const params = useParams()
  const navigate = useNavigate()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [template, setTemplate] = useState<ApplicationTemplateDto>()
  const [profile, setProfile] = useState<ProfileDto>()
  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()

  useEffect(() => {
    async function loadData() {
      const templateId = params.id ? parseInt(params.id) : undefined
      if (templateId) {
        try {
          const styles = await CvApi.getCVStyles(i18n.language)
          setCvStyles(styles)
        } catch (e) {
          const error = (e as RestError).errorDto
          addErrorToast(t('cv.styleError'), error?.message ?? t('error.genericMessage'))
        }

        try {
          const result = await ProfileApi.getProfile(i18n.language)
          setProfile(result)
        } catch (e) {
          const error = (e as RestError).errorDto
          addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
        }

        try {
          const result = await ApplicationTemplateApi.getApplicationTemplate(
            templateId,
            i18n.language
          )
          setTemplate(result)
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
      }
    }
    loadData()
  }, [])

  function handleDelete() {
    navigate(getRoutePath(RouteId.ApplicationTemplateOverview))
  }

  return (
    <LoadingWrapper isLoading={isLoading}>
      <div className="lg:max-w-3/4 2xl:max-w-350 w-full">
        <Button
          as={Link}
          to={getRoutePath(RouteId.ApplicationTemplateOverview)}
          color="default"
          variant="light"
          className="self-start"
          startContent={<FaArrowLeft />}
        >
          {t('applicationTemplate.backToOverview')}
        </Button>
        {template && profile && cvStyles ? (
          <div className="flex flex-col gap-10">
            <h1 className={`${h1()} self-center`}>{template.name}</h1>
            <div className="flex flex-wrap self-end gap-5 justify-end">
              <Button
                color="primary"
                startContent={<FaPen />}
                as={Link}
                to={getRoutePath(
                  RouteId.EditApplicationTemplate,
                  undefined,
                  template.id.toString()
                )}
              >
                {t('crud.edit')}
              </Button>
              <DeleteApplicationTemplateModal
                id={template.id}
                trigger={
                  <Button startContent={<FaTrash />} color="danger">
                    {t('crud.delete')}
                  </Button>
                }
                onDelete={handleDelete}
              />
            </div>

            <CvConfigurationEditor
              profile={profile}
              cvStyles={cvStyles}
              config={toConfigData(template.cvConfiguration)}
              disabled
            />
            {template.documentChecklist && (
              <div>
                <h4 className={h4()}>{t('applicationTemplate.documentChecklist')}</h4>
                <p className="text-default-400">{t('applicationTemplate.documentChecklistHint')}</p>
                <div className="flex flex-col gap-2 mt-2">
                  {template.documentChecklist.map((doc) => (
                    <CheckboxInput key={doc} label={doc} isDisabled />
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : (
          <Empty headline={t('applicationTemplate.notFound')} />
        )}
      </div>
    </LoadingWrapper>
  )
}

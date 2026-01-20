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
} from '@/components/download/cv/CvConfigurationEditor.tsx'
import { SelectedCvContent } from '@/components/download/cv/CvContentTreeRoot.tsx'
import CvApi from '@/api/CvApi.ts'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import CoverLetterApi from '@/api/CoverLetterApi.ts'
import { CoverLetterConfigurationDto } from '@/types/applicationTemplate/CoverLetterConfigurationDto.ts'
import { Attribute, AttributeList } from '@/components/AttributeList.tsx'
import { TFunction } from 'i18next'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'
import { CoverLetterGallery } from '@/components/download/coverletter/CoverLetterGallery.tsx'

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

function getCoverLetterAttributes(config: CoverLetterConfigurationDto, t: TFunction): Attribute[] {
  const attributes: Attribute[] = [
    {
      name: t('fields.language'),
      value: config.language
    },
    {
      name: t('coverLetter.mirrorProfileImage'),
      value: config.mirrorProfileImage ? t('general.yes') : t('general.no')
    },
    {
      name: t('coverLetter.content'),
      value: <p className="whitespace-break-spaces">{config.content}</p>
    },
    {
      name: t('fields.closing'),
      value: config.closing
    }
  ]

  if (config.documents) {
    attributes.push({
      name: t('applicationTemplate.applicationDocuments'),
      value: (
        <div className="flex flex-col gap-2 mt-2">
          {config.documents.map((doc) => (
            <CheckboxInput key={doc} label={doc} isDisabled />
          ))}
        </div>
      )
    })
  }
  return attributes
}

export function ApplicationTemplateDetailsPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const params = useParams()
  const navigate = useNavigate()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [template, setTemplate] = useState<ApplicationTemplateDto>()
  const [profile, setProfile] = useState<ProfileDto>()
  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()
  const [coverLetterStyles, setCoverLetterStyles] = useState<CoverLetterStyleDto[]>()

  useEffect(() => {
    async function loadData() {
      const templateId = params.id ? Number.parseInt(params.id) : undefined
      if (templateId) {
        try {
          const styles = await CvApi.getCVStyles(i18n.language)
          setCvStyles(styles)
        } catch (e) {
          const error = (e as RestError).errorDto
          addErrorToast(t('cv.styleError'), error?.message ?? t('error.genericMessage'))
        }

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
        {template && profile && cvStyles && coverLetterStyles ? (
          <div className="flex flex-col gap-10 items-center">
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

            <h4 className={h4()}>{t('cv.name')}</h4>
            <CvConfigurationEditor
              profile={profile}
              cvStyles={cvStyles}
              config={toConfigData(template.cvConfiguration)}
              disabled
            />

            <h4 className={h4()}>{t('coverLetter.name')}</h4>
            <CoverLetterGallery
              styles={coverLetterStyles}
              selectedStyle={template.coverLetterConfiguration.style}
              disabled
            />
              <AttributeList
                className="xl:max-w-3/4 gap-x-5"
                attributes={getCoverLetterAttributes(template.coverLetterConfiguration, t)}
              />
          </div>
        ) : (
          <Empty headline={t('applicationTemplate.notFound')} />
        )}
      </div>
    </LoadingWrapper>
  )
}

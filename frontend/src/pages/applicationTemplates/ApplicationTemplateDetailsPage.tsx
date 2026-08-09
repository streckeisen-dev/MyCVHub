import { Button } from '@/components/ui/Button.tsx'
import { PropsWithChildren, ReactNode, useEffect, useState } from 'react'
import { LoadingWrapper } from '@/layouts/LoadingWrapper.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { useTranslation } from 'react-i18next'
import { RestError } from '@/types/RestError.ts'
import { Empty } from '@/components/Empty.tsx'

import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { FaArrowLeft, FaPen, FaTrash } from 'react-icons/fa6'
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
import { Page, PageHeader, PageTitle, SectionTitle } from '@/components/ui/Layout.tsx'

type TemplateSectionProps = Readonly<
  PropsWithChildren & {
    title: string
  }
>

function TemplateSection(props: TemplateSectionProps): ReactNode {
  const { title, children } = props

  return (
    <section className="flex w-full flex-col gap-5 border-t border-default-200 pt-6">
      <SectionTitle>{title}</SectionTitle>
      {children}
    </section>
  )
}

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
      <Page size="wide">
        <Button
          as={Link}
          to={getRoutePath(RouteId.ApplicationTemplateOverview)}
          variant="tertiary"
          className="self-start"
        >
          <FaArrowLeft />
          {t('applicationTemplate.backToOverview')}
        </Button>
        {template && profile && cvStyles && coverLetterStyles ? (
          <div className="flex w-full flex-col gap-7">
            <PageHeader className="gap-4 md:flex-row md:items-start md:justify-between">
              <PageTitle>{template.name}</PageTitle>
              <div className="flex shrink-0 flex-wrap gap-3">
                <Button
                  variant="primary"
                  as={Link}
                  to={getRoutePath(
                    RouteId.EditApplicationTemplate,
                    undefined,
                    template.id.toString()
                  )}
                >
                  <FaPen />
                  {t('crud.edit')}
                </Button>
                <DeleteApplicationTemplateModal
                  id={template.id}
                  trigger={
                    <Button variant="danger">
                      <FaTrash />
                      {t('crud.delete')}
                    </Button>
                  }
                  onDelete={handleDelete}
                />
              </div>
            </PageHeader>

            <TemplateSection title={t('cv.name')}>
              <CvConfigurationEditor
                profile={profile}
                cvStyles={cvStyles}
                config={toConfigData(template.cvConfiguration)}
                disabled
              />
            </TemplateSection>

            <TemplateSection title={t('coverLetter.name')}>
              <div className="grid w-full max-w-5xl gap-6 xl:grid-cols-[minmax(18rem,24rem)_minmax(20rem,28rem)] xl:items-start">
                <CoverLetterGallery
                  styles={coverLetterStyles}
                  selectedStyle={template.coverLetterConfiguration.style}
                  disabled
                />
                <AttributeList
                  className="rounded-lg border border-default-200 bg-surface p-5"
                  attributes={getCoverLetterAttributes(template.coverLetterConfiguration, t)}
                />
              </div>
            </TemplateSection>
          </div>
        ) : (
          <Empty headline={t('applicationTemplate.notFound')} />
        )}
      </Page>
    </LoadingWrapper>
  )
}

import { ReactNode } from 'react'
import { Autocomplete, AutocompleteItem, Button } from '@heroui/react'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { CVStyleDto, CVStyleOptionDto } from '@/types/cv/CVStyleDto.ts'
import {
  CvContent,
  CvContentCustomizationView
} from '@/components/download/cv/CvContentCustomizationView.tsx'
import { FaSliders } from 'react-icons/fa6'
import { CvStyleCustomizationView } from '@/components/download/cv/CvStyleCustomizationView.tsx'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import talendoCvStyle from '@/assets/cv_styles/talendo.jpg'
import modernCvStyle from '@/assets/cv_styles/modern.jpg'
import { useTranslation } from 'react-i18next'
import { WorkExperienceDto } from '@/types/profile/workExperience/WorkExperienceDto.ts'
import { EducationDto } from '@/types/profile/education/EducationDto.ts'
import { ProjectDto } from '@/types/profile/project/ProjectDto.ts'
import { SelectedCvContent } from '@/components/download/cv/CvContentTreeRoot.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import { Key } from '@react-types/shared'
import { SelectableGallery } from '@/components/SelectableGallery.tsx'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { extractNestedErrors } from '@/helpers/FormHelper.ts'

const cvStyleImages: KeyValueObject<string> = {
  talendo: talendoCvStyle,
  modern: modernCvStyle
}

export interface CvConfigurationData {
  cvStyle: string | undefined
  cvContent: CvContent | undefined
  cvStyleOptions: KeyValueObject<string> | undefined
}

export type CvConfigurationEditorProps = Readonly<{
  profile: ProfileDto
  cvStyles: CVStyleDto[]
  config: CvConfigurationData
  templates?: ApplicationTemplateDto[]
  onChange?: (config: CvConfigurationData) => void
  disabled?: boolean
  errorMessages?: ErrorMessages
}>

function getDefaultStyleOptions(options: CVStyleOptionDto[]): KeyValueObject<string> {
  const data: KeyValueObject<string> = {}
  for (const option of options) {
    data[option.key] = option.default
  }
  return data
}

function toSelectedCvContent(o: WorkExperienceDto | EducationDto | ProjectDto): SelectedCvContent {
  return {
    id: o.id,
    includeDescription: true
  }
}

export function CvConfigurationEditor(props: CvConfigurationEditorProps): ReactNode {
  const { cvStyles, profile, config, templates, onChange, disabled = false, errorMessages } = props
  const { t } = useTranslation()

  const selectedCvStyle = cvStyles.find((s) => s.key === config.cvStyle)
  const cvStyleOptionErrors: ErrorMessages = errorMessages
    ? extractNestedErrors(errorMessages, 'cvStyleOptions.')
    : {}
  const includedCvContentErrors: ErrorMessages = errorMessages
    ? extractNestedErrors(errorMessages, 'includedCvContent.')
    : {}

  function handleStyleSelected(styleKey: string) {
    if (disabled || !onChange) return
    const style = cvStyles.find((s) => s.key === styleKey)
    if (!style) return

    onChange({
      ...config,
      cvStyle: style.key,
      cvStyleOptions: config.cvStyleOptions ? getDefaultStyleOptions(style.options) : undefined
    })
  }

  function toggleCustomizeContent() {
    if (disabled || !onChange) return
    onChange({
      ...config,
      cvContent: config.cvContent
        ? undefined
        : {
            workExperience: profile.workExperiences.map(toSelectedCvContent),
            education: profile.education.map(toSelectedCvContent),
            projects: profile.projects.map(toSelectedCvContent),
            skills: profile.skills.map((s) => s.id)
          }
    })
  }

  function handleToggleCvStyleOptions() {
    if (disabled || !onChange) return
    onChange({
      ...config,
      cvStyleOptions: config.cvStyleOptions
        ? undefined
        : getDefaultStyleOptions(selectedCvStyle?.options ?? [])
    })
  }

  function handleContentChange(content: CvContent) {
    if (disabled || !onChange) return
    onChange({
      ...config,
      cvContent: content
    })
  }

  function handleStyleOptionsChange(nane: string, value: string) {
    if (disabled || !onChange) return
    onChange({
      ...config,
      cvStyleOptions: {
        ...config.cvStyleOptions,
        [nane]: value
      }
    })
  }

  function handleTemplateChange(templateKey: Key | null) {
    if (!templates || templateKey == null) return
    const template = templates.find((t) => t.id === Number.parseInt(templateKey as string))
    if (template && onChange) {
      onChange({
        cvStyle: template.cvConfiguration.cvStyle,
        cvStyleOptions: template.cvConfiguration.cvStyleOptions,
        cvContent: template.cvConfiguration.includedCvContent
          ? {
              workExperience: template.cvConfiguration.includedCvContent.includedWorkExperience,
              education: template.cvConfiguration.includedCvContent.includedEducation,
              projects: template.cvConfiguration.includedCvContent.includedProjects,
              skills: template.cvConfiguration.includedCvContent.includedSkills
            }
          : undefined
      })
    }
  }

  return (
    <div className="w-full flex flex-col gap-2">
      {templates && (
        <div className="w-fit 2xl:pl-5">
          <Autocomplete
            name="applicationTemplate"
            label={t('applicationTemplate.singular')}
            onSelectionChange={handleTemplateChange}
          >
            {templates.map((template) => (
              <AutocompleteItem key={template.id}>{template.name}</AutocompleteItem>
            ))}
          </Autocomplete>
        </div>
      )}
      <SelectableGallery
        items={cvStyles.map((style) => ({
          key: style.key,
          name: style.name,
          image: cvStyleImages[style.key],
          alt: t('cv.imageAlt', { styleName: style.name }),
          description: style.description
        }))}
        selected={selectedCvStyle?.key}
        disabled={disabled}
        onSelect={handleStyleSelected}
      />
      {errorMessages?.cvStyle && <p className="text-danger text-sm">{errorMessages.cvStyle}</p>}

      {config.cvStyle && (
        <div className="w-full grid grid-cols-1 lg:grid-cols-2 gap-4">
          {((disabled && config.cvContent != null) || !disabled) && (
            <div className="flex flex-col gap-2 items-center">
              <Button variant="light" startContent={<FaSliders />} onPress={toggleCustomizeContent}>
                {t('cv.customizeContent')}
              </Button>
              {errorMessages?.includedCvContent && (
                <p className="text-danger text-sm self-start">{errorMessages.includedCvContent}</p>
              )}
              {config.cvContent && (
                <CvContentCustomizationView
                  profile={profile}
                  value={config.cvContent}
                  onChange={handleContentChange}
                  disabled={disabled}
                  errorMessages={includedCvContentErrors}
                />
              )}
            </div>
          )}
          {selectedCvStyle &&
            selectedCvStyle.options.length > 0 &&
            ((disabled && config.cvStyleOptions != null) || !disabled) && (
              <div className="flex flex-col gap-2 items-center">
                <Button
                  variant="light"
                  startContent={<FaSliders />}
                  onPress={handleToggleCvStyleOptions}
                >
                  {t('cv.customizeTemplate')}
                </Button>
                {config.cvStyleOptions && (
                  <CvStyleCustomizationView
                    options={selectedCvStyle.options}
                    value={config.cvStyleOptions}
                    onChange={handleStyleOptionsChange}
                    errorMessages={cvStyleOptionErrors}
                  />
                )}
              </div>
            )}
        </div>
      )}
    </div>
  )
}

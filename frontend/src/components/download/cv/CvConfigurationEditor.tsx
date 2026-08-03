import { AutocompleteItem, Autocomplete } from '@/components/ui/Fields.tsx'
import { Button } from '@/components/ui/Button.tsx'
import { Fragment, ReactNode } from 'react'

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
import { h4 } from '@/styles/primitives.ts'
import { CVStyleOptionType } from '@/types/cv/CVStyleOptionType.ts'

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
  compactGallery?: boolean
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
  const {
    cvStyles,
    profile,
    config,
    templates,
    compactGallery = false,
    onChange,
    disabled = false,
    errorMessages
  } = props
  const { t } = useTranslation()

  const selectedCvStyle = cvStyles.find((s) => s.key === config.cvStyle)
  const visibleCvStyles = disabled && selectedCvStyle ? [selectedCvStyle] : cvStyles
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

  function renderStyleOptionValue(option: CVStyleOptionDto): ReactNode {
    const value = config.cvStyleOptions?.[option.key] ?? option.default
    if (option.type === CVStyleOptionType.COLOR) {
      return (
        <span className="inline-flex items-center gap-2">
          <span
            className="h-5 w-5 rounded-sm border border-default-300"
            style={{ backgroundColor: value }}
          />
          <span>{value}</span>
        </span>
      )
    }
    return value
  }

  if (disabled) {
    return (
      <div className="grid w-full max-w-5xl gap-6 lg:grid-cols-[minmax(18rem,24rem)_minmax(20rem,36rem)] lg:items-start">
        <SelectableGallery
          items={visibleCvStyles.map((style) => ({
            key: style.key,
            name: style.name,
            image: cvStyleImages[style.key],
            alt: t('cv.imageAlt', { styleName: style.name }),
            description: style.description
          }))}
          selected={selectedCvStyle?.key}
          compact={compactGallery}
          disabled
          onSelect={handleStyleSelected}
        />

        <div className="flex w-full flex-col gap-5">
          {selectedCvStyle && selectedCvStyle.options.length > 0 && config.cvStyleOptions && (
            <div className="flex flex-col gap-2">
              <p className="text-sm font-semibold text-default-500">{t('cv.customizeTemplate')}</p>
              <dl className="grid w-full grid-cols-1 gap-x-6 gap-y-3 rounded-lg border border-default-200 bg-surface p-5 sm:grid-cols-[minmax(10rem,14rem)_minmax(0,1fr)]">
                {selectedCvStyle.options.map((option) => (
                  <Fragment key={option.key}>
                    <dt className="text-sm font-semibold text-default-500">{option.name}</dt>
                    <dd className="min-w-0 text-sm leading-6 text-foreground">
                      {renderStyleOptionValue(option)}
                    </dd>
                  </Fragment>
                ))}
              </dl>
            </div>
          )}

          {config.cvContent && (
            <div className="flex flex-col gap-2">
              <p className="text-sm font-semibold text-default-500">{t('cv.customizeContent')}</p>
              <CvContentCustomizationView
                profile={profile}
                value={config.cvContent}
                onChange={handleContentChange}
                disabled
                errorMessages={includedCvContentErrors}
              />
            </div>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="w-full flex flex-col gap-5 items-start">
      {templates && (
        <div className="w-fit 2xl:pl-5">
          <Autocomplete
            name="applicationTemplate"
            label={t('applicationTemplate.singular')}
            onSelectionChange={handleTemplateChange}
            description={t('applicationTemplate.usageHint')}
          >
            {templates.map((template) => (
              <AutocompleteItem key={template.id} id={template.id}>
                {template.name}
              </AutocompleteItem>
            ))}
          </Autocomplete>
        </div>
      )}
      {!disabled && !compactGallery && <h4 className={h4()}>{t('cv.stylesHeading')}</h4>}
      <div className="w-full max-w-5xl">
        <SelectableGallery
          items={visibleCvStyles.map((style) => ({
            key: style.key,
            name: style.name,
            image: cvStyleImages[style.key],
            alt: t('cv.imageAlt', { styleName: style.name }),
            description: style.description
          }))}
          selected={selectedCvStyle?.key}
          compact={compactGallery}
          disabled={disabled}
          onSelect={handleStyleSelected}
        />
      </div>
      {errorMessages?.cvStyle && <p className="text-danger text-sm">{errorMessages.cvStyle}</p>}

      {config.cvStyle && (
        <div className="flex w-full max-w-5xl flex-col gap-4">
          <div className="flex flex-wrap gap-3">
            {!disabled && (
              <Button
                className={
                  config.cvContent
                    ? 'border-accent bg-accent/10 text-accent hover:bg-accent/15'
                    : undefined
                }
                variant="secondary"
                onPress={toggleCustomizeContent}
              >
                <FaSliders />
                {t('cv.customizeContent')}
              </Button>
            )}
            {!disabled && selectedCvStyle && selectedCvStyle.options.length > 0 && (
              <Button
                className={
                  config.cvStyleOptions
                    ? 'border-accent bg-accent/10 text-accent hover:bg-accent/15'
                    : undefined
                }
                variant="secondary"
                onPress={handleToggleCvStyleOptions}
              >
                <FaSliders />
                {t('cv.customizeTemplate')}
              </Button>
            )}
          </div>

          {errorMessages?.includedCvContent && (
            <p className="text-danger text-sm self-start">{errorMessages.includedCvContent}</p>
          )}

          {((disabled && config.cvContent != null) || !disabled) && (
            <div
              className={
                config.cvContent
                  ? 'flex flex-col overflow-hidden rounded-lg border border-default-200 bg-surface'
                  : 'hidden'
              }
            >
              {disabled ? (
                <p className="border-b border-default-200 px-5 py-3 text-sm font-semibold text-default-500">
                  {t('cv.customizeContent')}
                </p>
              ) : (
                <p className="border-b border-default-200 px-5 py-3 text-sm font-semibold text-default-700">
                  {t('cv.customizeContent')}
                </p>
              )}
              {config.cvContent && (
                <div className="p-3 sm:p-4">
                  <CvContentCustomizationView
                    profile={profile}
                    value={config.cvContent}
                    onChange={handleContentChange}
                    disabled={disabled}
                    errorMessages={includedCvContentErrors}
                  />
                </div>
              )}
            </div>
          )}

          {selectedCvStyle &&
            selectedCvStyle.options.length > 0 &&
            ((disabled && config.cvStyleOptions != null) || !disabled) && (
              <div
                className={
                  config.cvStyleOptions
                    ? 'flex max-w-xl flex-col overflow-hidden rounded-lg border border-default-200 bg-surface'
                    : 'hidden'
                }
              >
                {disabled ? (
                  <p className="border-b border-default-200 px-5 py-3 text-sm font-semibold text-default-500">
                    {t('cv.customizeTemplate')}
                  </p>
                ) : (
                  <p className="border-b border-default-200 px-5 py-3 text-sm font-semibold text-default-700">
                    {t('cv.customizeTemplate')}
                  </p>
                )}
                {config.cvStyleOptions &&
                  (disabled ? (
                    <dl className="grid w-full grid-cols-1 gap-x-6 gap-y-3 p-5 sm:grid-cols-[minmax(10rem,14rem)_minmax(0,1fr)]">
                      {selectedCvStyle.options.map((option) => (
                        <Fragment key={option.key}>
                          <dt className="text-sm font-semibold text-default-500">{option.name}</dt>
                          <dd className="min-w-0 text-sm leading-6 text-foreground">
                            {renderStyleOptionValue(option)}
                          </dd>
                        </Fragment>
                      ))}
                    </dl>
                  ) : (
                    <div className="p-4">
                      <CvStyleCustomizationView
                        options={selectedCvStyle.options}
                        value={config.cvStyleOptions}
                        onChange={handleStyleOptionsChange}
                        errorMessages={cvStyleOptionErrors}
                      />
                    </div>
                  ))}
              </div>
            )}
        </div>
      )}
    </div>
  )
}

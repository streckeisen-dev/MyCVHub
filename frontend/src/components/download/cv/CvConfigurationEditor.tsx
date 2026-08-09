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
import { TFunction } from 'i18next'

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

function getCvStyleGalleryItems(cvStyles: CVStyleDto[], t: TFunction) {
  return cvStyles.map((style) => ({
    key: style.key,
    name: style.name,
    image: cvStyleImages[style.key],
    alt: t('cv.imageAlt', { styleName: style.name }),
    description: style.description
  }))
}

function renderStyleOptionValue(option: CVStyleOptionDto, value: string): ReactNode {
  if (option.type !== CVStyleOptionType.COLOR) return value

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

type CvStyleOptionSummaryProps = Readonly<{
  options: CVStyleOptionDto[]
  values: KeyValueObject<string>
}>

function CvStyleOptionSummary(props: CvStyleOptionSummaryProps): ReactNode {
  const { options, values } = props

  return (
    <dl className="grid w-full grid-cols-1 gap-x-6 gap-y-3 rounded-lg border border-default-200 bg-surface p-5 sm:grid-cols-[minmax(10rem,14rem)_minmax(0,1fr)]">
      {options.map((option) => (
        <Fragment key={option.key}>
          <dt className="text-sm font-semibold text-default-500">{option.name}</dt>
          <dd className="min-w-0 text-sm leading-6 text-foreground">
            {renderStyleOptionValue(option, values[option.key] ?? option.default)}
          </dd>
        </Fragment>
      ))}
    </dl>
  )
}

type DisabledCvConfigurationViewProps = Readonly<{
  compactGallery: boolean
  config: CvConfigurationData
  includedCvContentErrors: ErrorMessages
  profile: ProfileDto
  selectedCvStyle: CVStyleDto | undefined
  t: TFunction
  visibleCvStyles: CVStyleDto[]
  onContentChange: (content: CvContent) => void
  onStyleSelected: (styleKey: string) => void
}>

function DisabledCvConfigurationView(props: DisabledCvConfigurationViewProps): ReactNode {
  const {
    compactGallery,
    config,
    includedCvContentErrors,
    profile,
    selectedCvStyle,
    t,
    visibleCvStyles,
    onContentChange,
    onStyleSelected
  } = props

  return (
    <div className="grid w-full max-w-5xl gap-6 lg:grid-cols-[minmax(18rem,24rem)_minmax(20rem,36rem)] lg:items-start">
      <SelectableGallery
        items={getCvStyleGalleryItems(visibleCvStyles, t)}
        selected={selectedCvStyle?.key}
        compact={compactGallery}
        disabled
        onSelect={onStyleSelected}
      />

      <div className="flex w-full flex-col gap-5">
        {selectedCvStyle && selectedCvStyle.options.length > 0 && config.cvStyleOptions && (
          <div className="flex flex-col gap-2">
            <p className="text-sm font-semibold text-default-500">{t('cv.customizeTemplate')}</p>
            <CvStyleOptionSummary options={selectedCvStyle.options} values={config.cvStyleOptions} />
          </div>
        )}

        {config.cvContent && (
          <div className="flex flex-col gap-2">
            <p className="text-sm font-semibold text-default-500">{t('cv.customizeContent')}</p>
            <CvContentCustomizationView
              profile={profile}
              value={config.cvContent}
              onChange={onContentChange}
              disabled
              errorMessages={includedCvContentErrors}
            />
          </div>
        )}
      </div>
    </div>
  )
}

type ActiveCvConfigurationViewProps = Readonly<{
  compactGallery: boolean
  config: CvConfigurationData
  cvStyleOptionErrors: ErrorMessages
  errorMessages?: ErrorMessages
  includedCvContentErrors: ErrorMessages
  profile: ProfileDto
  selectedCvStyle: CVStyleDto | undefined
  t: TFunction
  templates?: ApplicationTemplateDto[]
  visibleCvStyles: CVStyleDto[]
  onContentChange: (content: CvContent) => void
  onStyleOptionsChange: (name: string, value: string) => void
  onStyleSelected: (styleKey: string) => void
  onTemplateChange: (templateKey: Key | null) => void
  onToggleContent: () => void
  onToggleStyleOptions: () => void
}>

function ActiveCvConfigurationView(props: ActiveCvConfigurationViewProps): ReactNode {
  const {
    compactGallery,
    config,
    cvStyleOptionErrors,
    errorMessages,
    includedCvContentErrors,
    profile,
    selectedCvStyle,
    t,
    templates,
    visibleCvStyles,
    onContentChange,
    onStyleOptionsChange,
    onStyleSelected,
    onTemplateChange,
    onToggleContent,
    onToggleStyleOptions
  } = props

  return (
    <div className="w-full flex flex-col gap-5 items-start">
      {templates && (
        <div className="w-fit 2xl:pl-5">
          <Autocomplete
            name="applicationTemplate"
            label={t('applicationTemplate.singular')}
            onSelectionChange={onTemplateChange}
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
      {!compactGallery && <h4 className={h4()}>{t('cv.stylesHeading')}</h4>}
      <div className="w-full max-w-5xl">
        <SelectableGallery
          items={getCvStyleGalleryItems(visibleCvStyles, t)}
          selected={selectedCvStyle?.key}
          compact={compactGallery}
          onSelect={onStyleSelected}
        />
      </div>
      {errorMessages?.cvStyle && <p className="text-danger text-sm">{errorMessages.cvStyle}</p>}

      {config.cvStyle && (
        <div className="flex w-full max-w-5xl flex-col gap-4">
          <div className="flex flex-wrap gap-3">
            <Button
              className={
                config.cvContent
                  ? 'border-accent bg-accent/10 text-accent hover:bg-accent/15'
                  : undefined
              }
              variant="secondary"
              onPress={onToggleContent}
            >
              <FaSliders />
              {t('cv.customizeContent')}
            </Button>
            {selectedCvStyle && selectedCvStyle.options.length > 0 && (
              <Button
                className={
                  config.cvStyleOptions
                    ? 'border-accent bg-accent/10 text-accent hover:bg-accent/15'
                    : undefined
                }
                variant="secondary"
                onPress={onToggleStyleOptions}
              >
                <FaSliders />
                {t('cv.customizeTemplate')}
              </Button>
            )}
          </div>

          {errorMessages?.includedCvContent && (
            <p className="text-danger text-sm self-start">{errorMessages.includedCvContent}</p>
          )}

          <div
            className={
              config.cvContent
                ? 'flex flex-col overflow-hidden rounded-lg border border-default-200 bg-surface'
                : 'hidden'
            }
          >
            <p className="border-b border-default-200 px-5 py-3 text-sm font-semibold text-default-700">
              {t('cv.customizeContent')}
            </p>
            {config.cvContent && (
              <div className="p-3 sm:p-4">
                <CvContentCustomizationView
                  profile={profile}
                  value={config.cvContent}
                  onChange={onContentChange}
                  disabled={false}
                  errorMessages={includedCvContentErrors}
                />
              </div>
            )}
          </div>

          {selectedCvStyle && selectedCvStyle.options.length > 0 && (
            <div
              className={
                config.cvStyleOptions
                  ? 'flex max-w-xl flex-col overflow-hidden rounded-lg border border-default-200 bg-surface'
                  : 'hidden'
              }
            >
              <p className="border-b border-default-200 px-5 py-3 text-sm font-semibold text-default-700">
                {t('cv.customizeTemplate')}
              </p>
              {config.cvStyleOptions && (
                <div className="p-4">
                  <CvStyleCustomizationView
                    options={selectedCvStyle.options}
                    value={config.cvStyleOptions}
                    onChange={onStyleOptionsChange}
                    errorMessages={cvStyleOptionErrors}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
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

  if (disabled) {
    return (
      <DisabledCvConfigurationView
        compactGallery={compactGallery}
        config={config}
        includedCvContentErrors={includedCvContentErrors}
        profile={profile}
        selectedCvStyle={selectedCvStyle}
        t={t}
        visibleCvStyles={visibleCvStyles}
        onContentChange={handleContentChange}
        onStyleSelected={handleStyleSelected}
      />
    )
  }

  return (
    <ActiveCvConfigurationView
      compactGallery={compactGallery}
      config={config}
      cvStyleOptionErrors={cvStyleOptionErrors}
      errorMessages={errorMessages}
      includedCvContentErrors={includedCvContentErrors}
      profile={profile}
      selectedCvStyle={selectedCvStyle}
      t={t}
      templates={templates}
      visibleCvStyles={visibleCvStyles}
      onContentChange={handleContentChange}
      onStyleOptionsChange={handleStyleOptionsChange}
      onStyleSelected={handleStyleSelected}
      onTemplateChange={handleTemplateChange}
      onToggleContent={toggleCustomizeContent}
      onToggleStyleOptions={handleToggleCvStyleOptions}
    />
  )
}

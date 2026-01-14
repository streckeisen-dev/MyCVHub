import { ReactNode, useState } from 'react'
import { Button, Card, CardBody, CardFooter, CardHeader } from '@heroui/react'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { CVStyleDto, CVStyleOptionDto } from '@/types/cv/CVStyleDto.ts'
import {
  CvContent,
  CvContentCustomizationView
} from '@/components/cv/CvContentCustomizationView.tsx'
import { FaSliders } from 'react-icons/fa6'
import { CvStyleCustomizationView } from '@/components/cv/CvStyleCustomizationView.tsx'
import sanitizeHtml from 'sanitize-html'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import talendoCvStyle from '@/assets/cv_styles/talendo.jpg'
import modernCvStyle from '@/assets/cv_styles/modern.jpg'
import { useTranslation } from 'react-i18next'
import { WorkExperienceDto } from '@/types/profile/workExperience/WorkExperienceDto.ts'
import { EducationDto } from '@/types/profile/education/EducationDto.ts'
import { ProjectDto } from '@/types/profile/project/ProjectDto.ts'
import { SelectedCvContent } from '@/components/cv/CvContentTreeRoot.tsx'

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
  onChange?: (config: CvConfigurationData) => void
  disabled?: boolean
}>

function sanitize(html: string): string {
  return sanitizeHtml(html, {
    allowedTags: ['p', 'a']
  })
}

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
  const { cvStyles, profile, config, onChange, disabled = false } = props
  const { t } = useTranslation()

  const [selectedCvStyle, setSelectedCvStyle] = useState<CVStyleDto | undefined>(cvStyles.find(style => style.key === config.cvStyle))

  function handleStyleSelected(style: CVStyleDto) {
    if (disabled || !onChange) return
    setSelectedCvStyle(style)
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

  return (
    <div className="w-full">
      <div className="flex flex-col sm:flex-row justify-center gap-4">
        {cvStyles.map((cvStyle) => (
          <Card
            key={cvStyle.key}
            className="w-full lg:max-w-lg p-2"
            style={{
              border:
                cvStyle.key === config.cvStyle ? '2px solid hsl(var(--heroui-primary))' : 'none'
            }}
          >
            <CardHeader>
              <p className="font-bold text-large">{cvStyle.name}</p>
            </CardHeader>
            <CardBody className="flex flex-col gap-2">
              <img src={cvStyleImages[cvStyle.key]} alt={`Example of ${cvStyle.name} CV style`} />
              <p
                className={'text-default-600'}
                dangerouslySetInnerHTML={{
                  __html: sanitize(cvStyle.description)
                }}
              />
            </CardBody>
            {profile && !disabled && (
              <CardFooter>
                <Button color="primary" onPress={() => handleStyleSelected(cvStyle)}>
                  {t('cv.select')}
                </Button>
              </CardFooter>
            )}
          </Card>
        ))}
      </div>

      {config.cvStyle && (
        <div className="w-full grid grid-cols-1 lg:grid-cols-2 gap-4">
          {((disabled && config.cvContent != null) || !disabled) && (
            <div className="flex flex-col gap-2 items-center">
              <Button variant="light" startContent={<FaSliders />} onPress={toggleCustomizeContent}>
                {t('cv.customizeContent')}
              </Button>
              {config.cvContent && (
                <CvContentCustomizationView
                  profile={profile}
                  value={config.cvContent}
                  onChange={handleContentChange}
                  disabled={disabled}
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
                  />
                )}
              </div>
            )}
        </div>
      )}
    </div>
  )
}

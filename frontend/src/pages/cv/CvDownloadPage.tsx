import { useTranslation } from 'react-i18next'
import { centerSection, h3, h4 } from '@/styles/primitives.ts'
import { useEffect, useState } from 'react'
import { CVStyleDto, CVStyleOptionDto } from '@/types/CVStyleDto.ts'
import { Button, Card, CardBody, CardFooter, CardHeader, Spinner } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import CvApi from '@/api/CvApi.ts'
import { ProfileDto } from '@/types/ProfileDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import talendoCvStyle from '@/assets/cv_styles/talendo.jpg'
import modernCvStyle from '@/assets/cv_styles/modern.jpg'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import sanitizeHtml from 'sanitize-html'
import { FaSliders } from 'react-icons/fa6'
import { CvContent, CvContentCustomizationView } from '@/components/cv/CvContentCustomizationView.tsx'
import { CvStyleCustomizationView } from '@/components/cv/CvStyleCustomizationView.tsx'
import { CVGenerationRequestDto } from '@/types/CVGenerationRequestDto.ts'
import { WorkExperienceDto } from '@/types/WorkExperienceDto.ts'
import { EducationDto } from '@/types/EducationDto.ts'
import { ProjectDto } from '@/types/ProjectDto.ts'
import { SkillDto } from '@/types/SkillDto.ts'
import { SelectedCvContent } from '@/components/cv/CvContentTreeRoot.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

function sanitize(html: string): string {
  return sanitizeHtml(html, {
    allowedTags: ['p', 'a']
  })
}

function getStyleDefaultOptions(options: CVStyleOptionDto[]): KeyValueObject<string> {
  const data: KeyValueObject<string> = {}
  for (const option of options) {
    data[option.key] = option.default
  }
  return data
}

function toSelectedCvContent(
  o: WorkExperienceDto | EducationDto | ProjectDto | SkillDto
): SelectedCvContent {
  return {
    id: o.id,
    includeDescription: true
  }
}

export function CvDownloadPage() {
  const { t, i18n } = useTranslation()

  const [cvStyles, setCvStyles] = useState<CVStyleDto[]>()
  const [isLoading, setIsLoading] = useState(true)
  const [profile, setProfile] = useState<ProfileDto>()
  const [selectedCvStyleKey, setSelectedCvStyleKey] = useState<string | undefined>()
  const selectedCvStyle: CVStyleDto | undefined = cvStyles?.find(
    (style) => style.key === selectedCvStyleKey
  )
  const [customizeContent, setCustomizeContent] = useState<boolean>(false)
  const [customizeTemplate, setCustomizeTemplate] = useState<boolean>(false)
  const [cvContent, setCvContent] = useState<CvContent>({
    workExperience: [],
    education: [],
    projects: [],
    skills: []
  })
  const [cvStyleOptions, setCvStyleOptions] = useState<KeyValueObject<string>>({})
  const [isGenerating, setIsGenerating] = useState<boolean>(false)

  const cvStyleImages: KeyValueObject<string> = {
    talendo: talendoCvStyle,
    modern: modernCvStyle
  }

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
        setCvContent({
          workExperience: result.workExperiences.map(toSelectedCvContent),
          education: result.education.map(toSelectedCvContent),
          projects: result.projects.map(toSelectedCvContent),
          skills: result.skills.map(toSelectedCvContent)
        })
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      }
    }

    loadCvStyles()
    loadProfile()
  }, [])

  function toggleCustomizeContent() {
    setCustomizeContent((prev) => {
      const newVal = !prev
      if (!newVal && profile) {
        setCvContent({
          workExperience: profile.workExperiences.map(toSelectedCvContent),
          education: profile.education.map(toSelectedCvContent),
          projects: profile.projects.map(toSelectedCvContent),
          skills: profile.skills.map(toSelectedCvContent)
        })
      }
      return newVal
    })
  }

  function handleStyleSelected(style: CVStyleDto) {
    setCvStyleOptions(getStyleDefaultOptions(style.options))
    setSelectedCvStyleKey(style.key)
  }

  function handleContentChange(content: CvContent) {
    setCvContent(content)
  }

  function handleStyleOptionChange(name: string, value: string) {
    setCvStyleOptions((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
  }

  async function handleDownload() {
    if (!selectedCvStyleKey) {
      return
    }
    const hasCvItems =
      cvContent.projects.length > 0 ||
      cvContent.workExperience.length > 0 ||
      cvContent.education.length > 0 ||
      cvContent.skills.length > 0
    if (customizeContent && !hasCvItems) {
      addErrorToast(t('cv.noItemsSelected'))
      return
    }

    setIsGenerating(true)
    const request: CVGenerationRequestDto = {
      includedWorkExperience: cvContent.workExperience,
      includedEducation: cvContent.education,
      includedProjects: cvContent.projects,
      includedSkills: cvContent.skills,
      templateOptions: cvStyleOptions
    }
    try {
      const data = await CvApi.getCV(selectedCvStyleKey, request, i18n.language)
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

  const content = cvStyles ? (
    <section className={centerSection()}>
      <h3 className={h3()}>{t('cv.generate')}</h3>
      <p>{t('cv.intro')}</p>
      <h4 className={h4()}>{t('cv.style')}</h4>
      <p>{t('cv.styleExplanation')}</p>

      <div className="flex flex-col sm:flex-row justify-center gap-4">
        {cvStyles.map((cvStyle) => (
          <Card
            key={cvStyle.key}
            className="w-full lg:max-w-lg p-2"
            style={{
              border:
                cvStyle.key === selectedCvStyleKey ? '2px solid hsl(var(--heroui-primary))' : 'none'
            }}
          >
            <CardHeader>
              <p className="font-bold text-large">{cvStyle.name}</p>
            </CardHeader>
            <CardBody className="flex flex-col gap-2">
              <img src={cvStyleImages[cvStyle.key]} alt={`Example of ${cvStyle.name} CV style`} />
              <p
                className={/* eslint-disable-next-line @eslint-react/dom/no-dangerously-set-innerhtml */ 'text-default-600'}
                dangerouslySetInnerHTML={{
                  __html: sanitize(cvStyle.description)
                }}
              />
            </CardBody>
            {profile && (
              <CardFooter>
                <Button color="primary" onPress={() => handleStyleSelected(cvStyle)}>
                  {t('cv.select')}
                </Button>
              </CardFooter>
            )}
          </Card>
        ))}
      </div>

      {profile && selectedCvStyleKey && (
        <div className="grid grid-cols-1 sm:grid-cols-2 w-fit gap-4 min-w-1/2 justify-items-center">
          <div className="flex flex-col gap-2 items-center">
            <Button variant="light" startContent={<FaSliders />} onPress={toggleCustomizeContent}>
              {t('cv.customizeContent')}
            </Button>
            {customizeContent && (
              <CvContentCustomizationView
                content={{
                  workExperience: profile.workExperiences,
                  education: profile.education,
                  projects: profile.projects,
                  skills: profile.skills
                }}
                value={cvContent}
                onChange={handleContentChange}
              />
            )}
          </div>
          {selectedCvStyle && selectedCvStyle.options.length > 0 && (
            <div className="flex flex-col gap-2 items-center">
              <Button
                variant="light"
                startContent={<FaSliders />}
                onPress={() => setCustomizeTemplate((prev) => !prev)}
              >
                {t('cv.customizeTemplate')}
              </Button>
              {customizeTemplate && (
                <CvStyleCustomizationView
                  options={selectedCvStyle.options}
                  value={cvStyleOptions}
                  onChange={handleStyleOptionChange}
                />
              )}
            </div>
          )}
        </div>
      )}

      <Button
        color="primary"
        onPress={handleDownload}
        isLoading={isGenerating}
        isDisabled={selectedCvStyle == null}
      >
        {t('cv.download')}
      </Button>
    </section>
  ) : (
    <Empty headline={t('cv.styleError')} />
  )

  return isLoading ? <Spinner /> : content
}

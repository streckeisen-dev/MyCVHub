import { ReactNode } from 'react'
import { Button, Tab, Tabs } from '@heroui/react'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { useTranslation } from 'react-i18next'
import { FaArrowLeftLong } from 'react-icons/fa6'
import { h2 } from '@/styles/primitives.ts'
import { GeneralEditor } from '@/components/profile/editor/GeneralEditor.tsx'
import { WorkExperienceEditor } from '@/components/profile/editor/workExperience/WorkExperienceEditor.tsx'
import { Key } from '@react-types/shared'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { EducationEditor } from '@/components/profile/editor/education/EducationEditor.tsx'
import { ProjectEditor } from '@/components/profile/editor/project/ProjectEditor.tsx'
import { ThemeEditor } from '@/components/profile/editor/theme/ThemeEditor.tsx'
import { SkillEditor } from '@/components/profile/editor/skill/SkillEditor.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

interface TabConfiguration {
  key: Key
  title: string
  content: ReactNode
  show: boolean
}

const EMPTY_PROFILE: ProfileDto = {
  jobTitle: '',
  bio: '',
  isProfilePublic: false,
  isEmailPublic: false,
  isPhonePublic: false,
  isAddressPublic: false,
  hideDescriptions: true,
  profilePicture: '',
  workExperiences: [],
  education: [],
  skills: [],
  projects: [],
  theme: undefined
}

export type ProfileEditorProps = Readonly<{
  initialValue?: ProfileDto
  onSaved?: () => void
}>

export function ProfileEditor(props: ProfileEditorProps): ReactNode {
  const { t } = useTranslation()
  const { initialValue } = props
  const navigate = useNavigate()
  const profile = initialValue ?? EMPTY_PROFILE
  const location = useLocation()

  const tabConfig: TabConfiguration[] = [
    {
      key: 'general',
      title: t('profile.editor.general'),
      show: true,
      content: (
        <GeneralEditor
          initialValue={{
            profilePicture: profile.profilePicture,
            jobTitle: profile.jobTitle,
            bio: profile.bio,
            isProfilePublic: profile.isProfilePublic,
            isEmailPublic: profile.isEmailPublic,
            isPhonePublic: profile.isPhonePublic,
            isAddressPublic: profile.isAddressPublic,
            hideDescriptions: profile.hideDescriptions
          }}
        />
      )
    },
    {
      key: 'experience',
      title: t('workExperience.title'),
      show: initialValue != null,
      content: <WorkExperienceEditor initialValue={profile.workExperiences} />
    },
    {
      key: 'education',
      title: t('education.title'),
      show: initialValue != null,
      content: <EducationEditor initialValue={profile.education} />
    },
    {
      key: 'projects',
      title: t('project.title'),
      show: initialValue != null,
      content: <ProjectEditor initialValue={profile.projects} />
    },
    {
      key: 'skills',
      title: t('skills.title'),
      show: initialValue != null,
      content: <SkillEditor initialValue={profile.skills} />
    },
    {
      key: 'theme',
      title: t('theme.title'),
      show: initialValue != null,
      content: <ThemeEditor initialValue={profile.theme} />
    }
  ]

  const availableTabs = tabConfig.map((c) => c.key)

  const selectedTab = availableTabs.find((k) => k === location.hash?.replace('#', '')) ?? 'general'

  function handleTabChange(key: Key) {
    navigate(`#${key}`)
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <Button as={Link} to={getRoutePath(RouteId.Account)} startContent={<FaArrowLeftLong />}>
          {t('profile.editor.back')}
        </Button>
      </div>
      <h2 className={h2()}>{t('profile.title')}</h2>
      <Tabs onSelectionChange={handleTabChange} selectedKey={selectedTab}>
        {tabConfig
          .filter((config) => config.show)
          .map((config) => (
            <Tab key={config.key} title={config.title}>
              {config.content}
            </Tab>
          ))}
      </Tabs>
    </div>
  )
}

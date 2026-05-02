import { SkillDto } from '@/types/profile/skill/SkillDto.ts'
import { useTranslation } from 'react-i18next'
import { CvContentTreeRoot, SelectedCvContent } from '@/components/download/cv/CvContentTreeRoot.tsx'
import { SkillTreeRoot } from '@/components/download/cv/SkillTreeRoot.tsx'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'

export interface CvContent {
  workExperience: SelectedCvContent[]
  education: SelectedCvContent[]
  projects: SelectedCvContent[]
  skills: number[]
}

export type CvCustomizationViewProps = Readonly<{
  profile: ProfileDto
  value: CvContent
  onChange: (content: CvContent) => void
  disabled: boolean
  errorMessages?: ErrorMessages
}>

function groupSkills(
  groups: KeyValueObject<SkillDto[]>,
  skill: SkillDto
): KeyValueObject<SkillDto[]> {
  const group: SkillDto[] = groups[skill.type] || []
  group.push(skill)
  groups[skill.type] = group
  return groups
}

export function CvContentCustomizationView(props: CvCustomizationViewProps) {
  const { t } = useTranslation()
  const { profile, value, onChange, disabled, errorMessages } = props

  const groupedSkills = profile.skills.reduce((skills: KeyValueObject<SkillDto[]>, skill: SkillDto) => groupSkills(skills, skill), {})

  function handleWorkExperienceChange(experiences: SelectedCvContent[]) {
    onChange({
      ...value,
      workExperience: experiences
    })
  }

  function handleEducationChange(education: SelectedCvContent[]) {
    onChange({
      ...value,
      education: education
    })
  }

  function handleProjectChange(projects: SelectedCvContent[]) {
    onChange({
      ...value,
      projects: projects
    })
  }

  function handleSkillChange(skills: number[]) {
    onChange({
      ...value,
      skills: skills
    })
  }

  return (
    <div className="w-full rounded-lg p-2 sm:min-w-xs md:min-w-md lg:min-w-lg">
      {profile.workExperiences.length > 0 && (
        <CvContentTreeRoot
          title={t('workExperience.title')}
          content={profile.workExperiences.map((experience) => {
            const selected = value.workExperience.find((w) => w.id === experience.id)
            return {
              id: experience.id,
              title: `${experience.jobTitle} @ ${experience.company}`,
              selected: selected != null,
              includeDescription: selected?.includeDescription ?? true
            }
          })}
          onChange={handleWorkExperienceChange}
          disabled={disabled}
          errorMessage={errorMessages?.includedWorkExperience}
        />
      )}
      {profile.education.length > 0 && (
        <CvContentTreeRoot
          title={t('education.title')}
          content={profile.education.map((education) => {
            const selected = value.education.find((e) => e.id === education.id)
            return {
              id: education.id,
              title: `${education.degreeName} @ ${education.institution}`,
              selected: selected != null,
              includeDescription: selected?.includeDescription ?? true
            }
          })}
          onChange={handleEducationChange}
          disabled={disabled}
          errorMessage={errorMessages?.includedEducation}
        />
      )}
      {profile.projects.length > 0 && (
        <CvContentTreeRoot
          title={t('project.title')}
          content={profile.projects.map((project) => {
            const selected = value.projects.find((p) => p.id === project.id)
            return {
              id: project.id,
              title: project.name,
              selected: selected != null,
              includeDescription: selected?.includeDescription ?? true
            }
          })}
          onChange={handleProjectChange}
          disabled={disabled}
          errorMessage={errorMessages?.includedProjects}
        />
      )}
      {profile.skills.length > 0 && (
        <SkillTreeRoot
          content={Object.keys(groupedSkills).map((type) => {
            return {
              title: type,
              children: groupedSkills[type].map((skill) => {
                return {
                  id: skill.id,
                  title: skill.name,
                  selected: value.skills.includes(skill.id)
                }
              })
            }
          })}
          onChange={handleSkillChange}
          disabled={disabled}
          errorMessage={errorMessages?.includedSkills}
        />
      )}
    </div>
  )
}

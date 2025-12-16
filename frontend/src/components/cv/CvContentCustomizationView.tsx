import { WorkExperienceDto } from '@/types/WorkExperienceDto.ts'
import { EducationDto } from '@/types/EducationDto.ts'
import { ProjectDto } from '@/types/ProjectDto.ts'
import { SkillDto } from '@/types/SkillDto.ts'
import { useTranslation } from 'react-i18next'
import {
  CvContentTreeRoot,
  SelectedCvContent
} from '@/components/cv/CvContentTreeRoot.tsx'
import { SkillTreeRoot } from '@/components/cv/SkillTreeRoot.tsx'
import { KeyValueObject } from '@/types/KeyValueObject.ts'

export interface CvContent {
  workExperience: SelectedCvContent[];
  education: SelectedCvContent[];
  projects: SelectedCvContent[];
  skills: SelectedCvContent[];
}

export type CvCustomizationViewProps = Readonly<{
  content: {
    workExperience: WorkExperienceDto[];
    education: EducationDto[];
    projects: ProjectDto[];
    skills: SkillDto[];
  };
  value: CvContent;
  onChange: (content: CvContent) => void;
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
  const { content, value, onChange } = props

  const groupedSkills = content.skills.reduce(groupSkills, {})

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

  function handleSkillChange(skills: SelectedCvContent[]) {
    onChange({
      ...value,
      skills: skills
    })
  }

  return (
    <div className="w-full rounded-lg p-2 sm:min-w-xs md:min-w-md lg:min-w-lg xl:min-w-xl">
      {content.workExperience.length > 0 && (
        <CvContentTreeRoot
          title={t('workExperience.title')}
          content={content.workExperience.map((experience) => {
            const selected = value.workExperience.find(
              (w) => w.id === experience.id
            )
            return {
              id: experience.id,
              title: `${experience.jobTitle} @ ${experience.company}`,
              selected: selected != null,
              includeDescription: selected?.includeDescription ?? true
            }
          })}
          onChange={handleWorkExperienceChange}
        />
      )}
      {content.education.length > 0 && (
        <CvContentTreeRoot
          title={t('education.title')}
          content={content.education.map((education) => {
            const selected = value.education.find(
              (e) => e.id === education.id
            )
            return {
              id: education.id,
              title: `${education.degreeName} @ ${education.institution}`,
              selected: selected != null,
              includeDescription: selected?.includeDescription ?? true
            }
          })}
          onChange={handleEducationChange}
        />
      )}
      {content.projects.length > 0 && (
        <CvContentTreeRoot
          title={t('project.title')}
          content={content.projects.map((project) => {
            const selected = value.projects.find(
              (p) => p.id === project.id
            )
            return {
              id: project.id,
              title: project.name,
              selected: selected != null,
              includeDescription: selected?.includeDescription ?? true
            }
          })}
          onChange={handleProjectChange}
        />
      )}
      {content.skills.length > 0 && (
        <SkillTreeRoot
          content={Object.keys(groupedSkills).map((type) => {
            return {
              title: type,
              children: groupedSkills[type].map((skill) => {
                return {
                  id: skill.id,
                  title: skill.name,
                  selected: value.skills.some((s) => s.id === skill.id)
                }
              })
            }
          })}
          onChange={handleSkillChange}
        />
      )}
    </div>
  )
}

import { ReactNode } from 'react'
import { WorkExperienceDto } from '@/types/WorkExperienceDto.ts'
import { useTranslation } from 'react-i18next'
import {
  CvListEntry,
  ListModificationEvent
} from '@/pages/profile/editor/CvListEntry.tsx'
import { PublicWorkExperienceDto } from '@/types/PublicWorkExperienceDto.ts'
import { sortByStartAndEndDate } from '@/helpers/SortHelper.ts'

function sortExperience(
  a: WorkExperienceDto | PublicWorkExperienceDto,
  b: WorkExperienceDto | PublicWorkExperienceDto
): number {
  return sortByStartAndEndDate(
    {
      start: a.positionStart,
      end: a.positionEnd
    },
    {
      start: b.positionStart,
      end: b.positionEnd
    }
  )
}

export type WorkExperienceModificationEvent = (
  workExperience: WorkExperienceDto,
) => void;

export interface WorkExperienceListProps {
  workExperiences: WorkExperienceDto[] | PublicWorkExperienceDto[];
  hasActions?: boolean;
  onEdit?: WorkExperienceModificationEvent;
  onDelete?: ListModificationEvent;
}

export function WorkExperienceList(props: WorkExperienceListProps): ReactNode {
  const { t } = useTranslation()

  const handleEdit: ListModificationEvent = (id) => {
    const experience = props.workExperiences.find(
      (e) => (e as WorkExperienceDto).id === id
    )
    if (experience && props.onEdit) {
      props.onEdit(experience as WorkExperienceDto)
    }
  }

  return props.workExperiences.length ? (
    props.workExperiences.sort(sortExperience).map((experience) => (
      <CvListEntry
        key={(experience as WorkExperienceDto).id || `${experience.jobTitle}@${experience.company}`}
        value={{
          id: (experience as WorkExperienceDto).id,
          title: experience.jobTitle,
          topRight: experience.location,
          bottomLeft: experience.company,
          start: experience.positionStart,
          end: experience.positionEnd,
          description: experience.description
        }}
        onEdit={handleEdit}
        onDelete={props.onDelete}
        hasActions={props.hasActions}
      />
    ))
  ) : (
    <div className="w-full text-center">{t('workExperience.noEntries')}</div>
  )
}

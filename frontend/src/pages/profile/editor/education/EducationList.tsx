import { ReactNode } from 'react'
import { EducationDto } from '@/types/EducationDto.ts'
import { useTranslation } from 'react-i18next'
import {
  CvListEntry,
  ListModificationEvent
} from '@/pages/profile/editor/CvListEntry.tsx'
import { PublicEducationDto } from '@/types/PublicEducationDto.ts'
import { sortByStartAndEndDate } from '@/helpers/SortHelper.ts'

function sortEducation(
  a: EducationDto | PublicEducationDto,
  b: EducationDto | PublicEducationDto
): number {
  return sortByStartAndEndDate(
    {
      start: a.educationStart,
      end: a.educationEnd
    },
    {
      start: b.educationStart,
      end: b.educationEnd
    }
  )
}

export type EducationModificationEvent = (education: EducationDto) => void;

export interface EducationListProps {
  education: EducationDto[] | PublicEducationDto[];
  hasActions?: boolean;
  onEdit?: EducationModificationEvent;
  onDelete?: ListModificationEvent;
}

export function EducationList(props: EducationListProps): ReactNode {
  const { t } = useTranslation()

  const handleEdit: ListModificationEvent = (id) => {
    const education = props.education.find(
      (e) => (e as EducationDto).id === id
    )
    if (education && props.onEdit) {
      props.onEdit(education as EducationDto)
    }
  }

  return props.education.length === 0 ? (
    <div className="w-full text-center">{t('education.noEntries')}</div>
  ) : (
    props.education.sort(sortEducation).map((education) => (
      <CvListEntry
        key={(education as EducationDto).id || education.degreeName}
        hasActions={props.hasActions ?? false}
        value={{
          id: (education as EducationDto).id,
          title: education.institution,
          topRight: education.location,
          bottomLeft: education.degreeName,
          start: education.educationStart,
          end: education.educationEnd,
          description: education.description
        }}
        onEdit={handleEdit}
        onDelete={props.onDelete}
      />
    ))
  )
}

import { Fragment, ReactNode } from 'react'
import { SkillDto } from '@/types/SkillDto.ts'
import { useTranslation } from 'react-i18next'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import { PublicSkillDto } from '@/types/PublicSkillDto.ts'
import { h5 } from '@/styles/primitives.ts'
import { Button, Progress } from '@heroui/react'
import { FaPen, FaTrash } from 'react-icons/fa6'
import { ListModificationEvent } from '@/pages/profile/editor/CvListEntry.tsx'

function groupSkills(
  groups: KeyValueObject<(SkillDto | PublicSkillDto)[]>,
  skill: SkillDto | PublicSkillDto
): KeyValueObject<SkillDto[] | PublicSkillDto[]> {
  const group: SkillDto[] | PublicSkillDto[] = groups[skill.type] || []
  group.push(skill)
  groups[skill.type] = group
  return groups
}

function sortSkillTypes(
  groupedSkills: KeyValueObject<SkillDto[] | PublicSkillDto[]>
) {
  return (aKey: string, bKey: string) => {
    const aSum = getSkillSum(groupedSkills[aKey])
    const bSum = getSkillSum(groupedSkills[bKey])

    if (aSum > bSum) {
      return -1
    } else if (aSum < bSum) {
      return 1
    } else {
      return 0
    }
  }
}

function sortSkills(
  a: SkillDto | PublicSkillDto,
  b: SkillDto | PublicSkillDto
): number {
  if (a.level > b.level) {
    return -1
  } else if (a.level < b.level) {
    return 1
  } else {
    return 0
  }
}

export type SkillModificationEvent = (skill: SkillDto) => void;

export interface SkillListProps {
  skills: SkillDto[] | PublicSkillDto[];
  hasActions?: boolean;
  onEdit?: SkillModificationEvent;
  onDelete?: ListModificationEvent;
}

function getSkillSum(skills: SkillDto[] | PublicSkillDto[]): number {
  return skills.reduce((sum: number, skill: SkillDto | PublicSkillDto) => {
    return (sum += skill.level)
  }, 0)
}

export function SkillList(props: SkillListProps): ReactNode {
  const { t } = useTranslation()

  const groupedSkills: KeyValueObject<SkillDto[] | PublicSkillDto[]> =
    props.skills.reduce(groupSkills, {})

  return props.skills.length === 0 ? (
    <div className="w-full text-center">{t('skills.noEntires')}</div>
  ) : (
    Object.keys(groupedSkills)
      .sort(sortSkillTypes(groupedSkills))
      .map((skillType) => (
        <Fragment key={skillType}>
          <h5 className={h5()}>{skillType}</h5>
          {groupedSkills[skillType].sort(sortSkills).map((skill) => (
            <SkillEntry
              key={skill.name}
              skill={skill}
              hasActions={props.hasActions ?? false}
              onEdit={props.onEdit}
              onDelete={props.onDelete}
            />
          ))}
        </Fragment>
      ))
  )
}

interface SkillEntryProps {
  skill: SkillDto | PublicSkillDto;
  hasActions: boolean;
  onEdit: SkillModificationEvent | undefined;
  onDelete: ListModificationEvent | undefined;
}

function SkillEntry(props: SkillEntryProps): ReactNode {
  const { hasActions, skill, onEdit, onDelete } = props

  const nameClasses = hasActions
    ? 'col-span-4 lg:col-span-2'
    : 'col-span-6 lg:col-span-4'
  const levelClasses = hasActions
    ? 'col-span-8 lg:col-span-8'
    : 'col-span-6 lg:col-span-8'
  const buttonClasses =
    'col-span-2 md:col-span-1 w-min rounded-full lg:justify-self-end min-w-1'

  function handleEdit() {
    if (onEdit && (skill as SkillDto).id) {
      onEdit(skill as SkillDto)
    }
  }

  function handleDelete() {
    if (onDelete && (skill as SkillDto).id) {
      onDelete((skill as SkillDto).id)
    }
  }

  return (
    <div className="grid grid-cols-12 gap-x-2 gap-y-4 items-center">
      <p className={nameClasses}>{skill.name}</p>
      <Progress
        className={levelClasses}
        aria-label="Skill Level"
        value={skill.level}
      />
      {hasActions && (
        <>
          <Button
            color="primary"
            className={buttonClasses}
            onPress={handleEdit}
          >
            <FaPen />
          </Button>
          <Button
            color="danger"
            className={buttonClasses}
            onPress={handleDelete}
          >
            <FaTrash />
          </Button>
        </>
      )}
    </div>
  )
}

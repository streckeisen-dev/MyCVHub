import { ProjectDto } from '@/types/ProjectDto.ts'
import { useTranslation } from 'react-i18next'
import {
  CvListEntry,
  ListModificationEvent
} from '@/components/profile/editor/CvListEntry.tsx'
import { FaFile, FaGithub, FaGlobe, FaLink } from 'react-icons/fa6'
import { ProjectLinkType } from '@/types/ProjectLink.ts'
import { IconType } from 'react-icons'
import { Link } from '@heroui/react'
import { sortByStartAndEndDate } from '@/helpers/SortHelper.ts'
import { PublicProjectDto } from '@/types/PublicProjectDto.ts'

function sortProjects(
  a: ProjectDto | PublicProjectDto,
  b: ProjectDto | PublicProjectDto
): number {
  return sortByStartAndEndDate(
    {
      start: a.projectStart,
      end: a.projectEnd
    },
    {
      start: b.projectStart,
      end: b.projectEnd
    }
  )
}

export type ProjectModificationEvent = (project: ProjectDto) => void;

export interface ProjectListProps {
  projects: ProjectDto[] | PublicProjectDto[];
  hasActions?: boolean;
  onEdit?: ProjectModificationEvent;
  onDelete?: ListModificationEvent;
}

export function ProjectList(props: ProjectListProps) {
  const { t } = useTranslation()

  const handleEdit: ListModificationEvent = (id) => {
    const project = props.projects.find((p) => (p as ProjectDto).id === id)
    if (project && props.onEdit) {
      props.onEdit(project as ProjectDto)
    }
  }

  return props.projects.length === 0 ? (
    <div className="w-full text-center">{t('project.noEntries')}</div>
  ) : (
    props.projects.sort(sortProjects).map((project) => (
      <CvListEntry
        key={(project as ProjectDto).id || `${project.role}@${project.name}`}
        value={{
          id: (project as ProjectDto).id,
          title: project.name,
          topRight: project.links.map((link) => {
            let Icon: IconType
            switch (link.type) {
              case ProjectLinkType.DOCUMENT:
                Icon = FaFile
                break
              case ProjectLinkType.GITHUB:
                Icon = FaGithub
                break
              case ProjectLinkType.WEBSITE:
                Icon = FaGlobe
                break
              default:
                Icon = FaLink
                break
            }

            return (
              <Link key={1} isExternal color="foreground" href={link.url}>
                <Icon size={25} />
              </Link>
            )
          }),
          bottomLeft: project.role,
          start: project.projectStart,
          end: project.projectEnd,
          description: project.description
        }}
        hasActions={props.hasActions}
        onEdit={handleEdit}
        onDelete={props.onDelete}
      />
    ))
  )
}

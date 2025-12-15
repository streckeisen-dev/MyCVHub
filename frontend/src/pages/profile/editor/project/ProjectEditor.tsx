import { ReactNode, useState } from 'react'
import { ProjectDto } from '@/types/ProjectDto.ts'
import {
  EditProjectModal,
  ProjectFormData
} from '@/pages/profile/editor/project/EditProjectModal.tsx'
import { stringToCalendarDate } from '@/helpers/DateHelper.ts'
import { addToast, Button } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import {
  ProjectList,
  ProjectModificationEvent
} from '@/pages/profile/editor/project/ProjectList.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { v7 as uuid } from 'uuid'

function toFormData(project: ProjectDto): ProjectFormData {
  return {
    ...project,
    projectStart: stringToCalendarDate(project.projectStart),
    projectEnd: stringToCalendarDate(project.projectEnd),
    links: project.links.map((link) => {
      return {
        ...link,
        uiId: uuid()
      }
    })
  }
}

export type ProjectEditorProps = Readonly<{
  initialValue: ProjectDto[]
}>

export function ProjectEditor(props: ProjectEditorProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue } = props
  const [projects, setProjects] = useState<ProjectDto[]>(initialValue)
  const [editingProject, setEditingProject] = useState<ProjectFormData | undefined>()
  const [isEditing, setIsEditing] = useState(false)

  function handleAdd() {
    setIsEditing(true)
  }

  const handleEdit: ProjectModificationEvent = (project) => {
    setEditingProject(toFormData(project))
    setIsEditing(true)
  }

  async function handleDelete(id: number) {
    try {
      await ProfileApi.deleteProject(id, i18n.language)
      setProjects((prev) => prev.filter((p) => p.id !== id))
    } catch (e) {
      const error = (e as RestError).errorDto
      addToast({
        title: t('project.editor.deleteError'),
        description: error?.message ?? t('error.genericMessage'),
        color: 'danger'
      })
    }
  }

  function handleModalClose() {
    setIsEditing(false)
    setEditingProject(undefined)
  }

  function handleSaved(project: ProjectDto) {
    setProjects((prev) => [...prev.filter((p) => p.id !== project.id), project])
  }

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-col items-end">
        <Button color="primary" onPress={handleAdd}>
          {t('project.editor.add')}
        </Button>
      </div>
      <ProjectList projects={projects} hasActions onEdit={handleEdit} onDelete={handleDelete} />
      {isEditing && (
        <EditProjectModal
          isOpen={true}
          backdrop="blur"
          size="5xl"
          initialValue={editingProject}
          onSaved={handleSaved}
          onClose={handleModalClose}
        />
      )}
    </section>
  )
}

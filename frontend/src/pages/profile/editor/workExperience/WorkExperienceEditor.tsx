import { ReactNode, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@heroui/react'
import { WorkExperienceDto } from '@/types/WorkExperienceDto.ts'
import {
  WorkExperienceList,
  WorkExperienceModificationEvent
} from '@/pages/profile/editor/workExperience/WorkExperienceList.tsx'
import {
  EditWorkExperienceModal,
  WorkExperienceFormData
} from '@/pages/profile/editor/workExperience/EditWorkExperienceModal.tsx'
import { stringToCalendarDate } from '@/helpers/DateHelper.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export type WorkExperienceEditorProps = Readonly<{
  initialValue: WorkExperienceDto[]
}>

function toFormData(workExperience: WorkExperienceDto): WorkExperienceFormData {
  return {
    id: workExperience.id,
    company: workExperience.company,
    jobTitle: workExperience.jobTitle,
    description: workExperience.description,
    positionStart: stringToCalendarDate(workExperience.positionStart),
    positionEnd: stringToCalendarDate(workExperience.positionEnd),
    location: workExperience.location
  }
}

export function WorkExperienceEditor(props: WorkExperienceEditorProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue } = props
  const [workExperiences, setWorkExperiences] = useState<WorkExperienceDto[]>(initialValue)
  const [isEditing, setIsEditing] = useState(false)
  const [editingWorkExperience, setEditingWorkExperience] = useState<
    WorkExperienceFormData | undefined
  >()

  function handleAdd() {
    setIsEditing(true)
  }

  const handleEdit: WorkExperienceModificationEvent = (workExperience) => {
    setIsEditing(true)
    setEditingWorkExperience(toFormData(workExperience))
  }

  async function handleDelete(id: number) {
    try {
      await ProfileApi.deleteWorkExperience(id, i18n.language)
      setWorkExperiences((prev) => prev.filter((w) => w.id !== id))
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(
        t('workExperience.editor.deleteError'),
        error?.message ?? t('error.genericMessage')
      )
    }
  }

  function handleModalClose() {
    setEditingWorkExperience(undefined)
    setIsEditing(false)
  }

  function handleSaved(workExperience: WorkExperienceDto) {
    setWorkExperiences((prev) => {
      return [...prev.filter((w) => w.id !== workExperience.id), workExperience]
    })
  }

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-col items-end">
        <Button className="w-min" color="primary" onPress={handleAdd}>
          {t('workExperience.editor.add')}
        </Button>
      </div>
      <WorkExperienceList
        workExperiences={workExperiences}
        hasActions
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
      {isEditing && (
        <EditWorkExperienceModal
          isOpen={true}
          onClose={handleModalClose}
          initialValue={editingWorkExperience}
          onSaved={handleSaved}
        />
      )}
    </section>
  )
}

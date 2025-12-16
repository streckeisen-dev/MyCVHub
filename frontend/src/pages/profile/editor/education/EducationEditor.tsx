import { ReactNode, useState } from 'react'
import { Button } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import {
  EducationList,
  EducationModificationEvent
} from '@/pages/profile/editor/education/EducationList.tsx'
import { EducationDto } from '@/types/EducationDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import {
  EditEducationModal,
  EducationFormData
} from '@/pages/profile/editor/education/EditEducationModal.tsx'
import { stringToCalendarDate } from '@/helpers/DateHelper.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export type EducationEditorProps = Readonly<{
  initialValue: EducationDto[]
}>

function toFormData(education: EducationDto): EducationFormData {
  return {
    ...education,
    educationStart: stringToCalendarDate(education.educationStart),
    educationEnd: stringToCalendarDate(education.educationEnd),
    description: education.description ?? ''
  }
}

export function EducationEditor(props: EducationEditorProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue } = props

  const [education, setEducation] = useState<EducationDto[]>(initialValue)
  const [editingEducation, setEditingEducation] = useState<EducationFormData | undefined>()
  const [isEditing, setIsEditing] = useState(false)

  function handleAdd() {
    setIsEditing(true)
  }

  const handleEdit: EducationModificationEvent = (education) => {
    setEditingEducation(toFormData(education))
    setIsEditing(true)
  }

  async function handleDelete(id: number) {
    try {
      await ProfileApi.deleteEducation(id, i18n.language)
      setEducation((prev) => prev.filter((e) => e.id !== id))
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(t('education.editor.deleteError'), error?.message ?? t('error.genericMessage'))
    }
  }

  function handleSaved(education: EducationDto) {
    setEducation((prev) => {
      return [...prev.filter((w) => w.id !== education.id), education]
    })
  }

  function handleModalClose() {
    setIsEditing(false)
    setEditingEducation(undefined)
  }

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-col items-end">
        <Button className="w-min" color="primary" onPress={handleAdd}>
          {t('education.editor.add')}
        </Button>
      </div>
      <EducationList education={education} onEdit={handleEdit} onDelete={handleDelete} hasActions />
      {isEditing && (
        <EditEducationModal
          isOpen={true}
          size="5xl"
          onClose={handleModalClose}
          initialValue={editingEducation}
          onSaved={handleSaved}
        />
      )}
    </section>
  )
}

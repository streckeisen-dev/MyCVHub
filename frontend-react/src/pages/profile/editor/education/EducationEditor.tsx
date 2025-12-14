import { ReactNode, useState } from 'react'
import { addToast, Button } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import {
  EducationList,
  EducationModificationEvent
} from '@/pages/profile/editor/education/EducationList.tsx'
import { EducationDto } from '@/types/EducationDto.ts'
import { ListModificationEvent } from '@/pages/profile/editor/CvListEntry.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import {
  EditEducationModal,
  EducationFormData
} from '@/pages/profile/editor/education/EditEducationModal.tsx'
import { stringToCalendarDate } from '@/helpers/DateHelper.ts'

export interface EducationEditorProps {
  initialValue: EducationDto[];
}

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

  const handleDelete: ListModificationEvent = async (id) => {
    try {
      await ProfileApi.deleteEducation(id, i18n.language)
      setEducation((prev) => [...prev.filter((e) => e.id !== id)])
    } catch (e) {
      const error = (e as RestError).errorDto
      addToast({
        title: t('education.editor.deleteError'),
        description: error?.message ?? t('error.genericMessage'),
        color: 'danger'
      })
    }
  }

  function handleSaved(education: EducationDto) {
    setEducation((prev) => {
      return [...prev.filter((w) => w.id !== education.id), education]
    })
  }

  function handleModalClose() {
    console.log('CLOSE')
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
      <EducationList
        education={education}
        onEdit={handleEdit}
        onDelete={handleDelete}
        hasActions
      />
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

import { Button } from '@/components/ui/Button.tsx'
import { ReactNode, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { WorkExperienceDto } from '@/types/profile/workExperience/WorkExperienceDto.ts'
import {
  WorkExperienceList,
  WorkExperienceModificationEvent
} from '@/components/profile/editor/workExperience/WorkExperienceList.tsx'
import {
  EditWorkExperienceModal,
  WorkExperienceFormData
} from '@/components/profile/editor/workExperience/EditWorkExperienceModal.tsx'
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
  const [editor, setEditor] = useState<{
    careerBreak: boolean
    initialValue?: WorkExperienceFormData
  }>()

  const handleEdit: WorkExperienceModificationEvent = (workExperience) => {
    setEditor({
      careerBreak: workExperience.careerBreak ?? false,
      initialValue: toFormData(workExperience)
    })
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

  function handleSaved(workExperience: WorkExperienceDto) {
    setWorkExperiences((prev) => {
      return [...prev.filter((w) => w.id !== workExperience.id), workExperience]
    })
  }

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap justify-end gap-2">
        <Button variant="secondary" onPress={() => setEditor({ careerBreak: true })}>
          {t('workExperience.editor.addCareerBreak')}
        </Button>
        <Button variant="primary" onPress={() => setEditor({ careerBreak: false })}>
          {t('workExperience.editor.add')}
        </Button>
      </div>
      <WorkExperienceList
        workExperiences={workExperiences}
        hasActions
        onEdit={handleEdit}
        onDelete={handleDelete}
      />
      {editor && (
        <EditWorkExperienceModal
          isOpen={true}
          onClose={() => setEditor(undefined)}
          initialValue={editor.initialValue}
          careerBreak={editor.careerBreak}
          onSaved={handleSaved}
        />
      )}
    </section>
  )
}

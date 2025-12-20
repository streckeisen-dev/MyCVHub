import { ReactNode, useState } from 'react'
import { SkillDto } from '@/types/profile/skill/SkillDto.ts'
import { SkillList, SkillModificationEvent } from '@/components/profile/editor/skill/SkillList.tsx'
import { Button } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { EditSkillModal, SkillFormData } from '@/components/profile/editor/skill/EditSkillModal.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export type SkillEditorProps = Readonly<{
  initialValue: SkillDto[]
}>

export function SkillEditor(props: SkillEditorProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue } = props

  const [skills, setSkills] = useState<SkillDto[]>(initialValue)
  const [isEditing, setIsEditing] = useState(false)
  const [editingSkill, setEditingSkill] = useState<SkillFormData | undefined>()

  function handleAdd() {
    setIsEditing(true)
  }

  const handleEdit: SkillModificationEvent = (skill: SkillDto) => {
    setEditingSkill({ ...skill })
    setIsEditing(true)
  }

  function handleSaved(skill: SkillDto) {
    setSkills((prev) => [...prev.filter((s) => s.id !== skill.id), skill])
  }

  async function handleDelete(id: number) {
    try {
      await ProfileApi.deleteSkill(id, i18n.language)
      setSkills((prev) => prev.filter((s) => s.id !== id))
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(t('skills.editor.deleteError'), error?.message ?? t('error.genericMessage'))
    }
  }

  function handleModalClose() {
    setIsEditing(false)
    setEditingSkill(undefined)
  }

  return (
    <section className="flex flex-col gap-4">
      <div className="flex justify-end">
        <Button color="primary" onPress={handleAdd}>
          {t('skills.editor.add')}
        </Button>
      </div>
      <SkillList skills={skills} hasActions onEdit={handleEdit} onDelete={handleDelete} />
      {isEditing && (
        <EditSkillModal
          isOpen={true}
          initialValue={editingSkill}
          existingTypes={[...new Set(skills.map((s) => s.type))]}
          onClose={handleModalClose}
          onSaved={handleSaved}
        />
      )}
    </section>
  )
}

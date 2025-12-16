import { ReactNode } from 'react'
import { Button } from '@heroui/react'
import { useTranslation } from 'react-i18next'

export type FormButtonsProps = Readonly<{
  onCancel: () => void
  isSaving: boolean
}>

export function FormButtons(props: FormButtonsProps): ReactNode {
  const { t } = useTranslation()
  const { onCancel, isSaving } = props
  return (
    <div className="flex gap-3">
      <Button color="primary" type="submit" isLoading={isSaving} data-testid="save-button">
        {t('forms.save')}
      </Button>
      <Button color="default" onPress={onCancel} isDisabled={isSaving} data-testid="cancel-button">
        {t('forms.cancel')}
      </Button>
    </div>
  )
}

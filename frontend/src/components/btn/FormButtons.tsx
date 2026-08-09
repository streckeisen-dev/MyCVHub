import { Button } from '@/components/ui/Button.tsx'
import { ReactNode } from 'react'

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
      <Button variant="primary" type="submit" isPending={isSaving} data-testid="save-button">
        {t('forms.save')}
      </Button>
      <Button
        type="button"
        onPress={onCancel}
        isDisabled={isSaving}
        data-testid="cancel-button"
      >
        {t('forms.cancel')}
      </Button>
    </div>
  )
}

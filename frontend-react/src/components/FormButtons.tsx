import { ReactNode } from 'react'
import { Button } from '@heroui/react'
import { useTranslation } from 'react-i18next'

export interface FormButtonsProps {
  onCancel: () => void;
  isSaving: boolean
}

export function FormButtons(props: FormButtonsProps): ReactNode {
  const { t } = useTranslation()
  const { onCancel, isSaving } = props
  return (
    <div className="flex gap-3">
      <Button color="primary" type="submit" isLoading={isSaving}>{t('forms.save')}</Button>
      <Button color="default" onPress={onCancel} isDisabled={isSaving}>
        {t('forms.cancel')}
      </Button>
    </div>
  )
}

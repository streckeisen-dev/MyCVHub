import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import AccountApi from '@/api/AccountApi.ts'
import { Button } from '@heroui/react'
import { RestError } from '@/types/RestError.ts'
import { h3 } from '@/styles/primitives.ts'
import { addErrorToast, addSuccessToast } from '@/helpers/ToastHelper.ts'

export function UnverifiedView(): ReactNode {
  const { t, i18n } = useTranslation()

  async function handleGenerateToken() {
    try {
      await AccountApi.generateVerificationCode(i18n.language)
      addSuccessToast(t('account.verification.resend.success'))
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(
        t('account.verification.resend.error'),
        error?.message ?? t('error.genericMessage')
      )
    }
  }

  return (
    <div className="flex flex-col gap-4 items-center">
      <h3 className={h3()}>{t('account.verification.pending.title')}</h3>
      <p>{t('account.verification.pending.description')}</p>
      <p>
        {t('account.verification.resend.description')}
        <Button color="primary" variant="light" onPress={handleGenerateToken} size="lg">
          {t('account.verification.resend.action')}
        </Button>
      </p>
    </div>
  )
}

import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import AccountApi from '@/api/AccountApi.ts'
import { addToast, Button } from '@heroui/react'
import { RestError } from '@/types/RestError.ts'
import { h3 } from '@/styles/primitives.ts'

export function UnverifiedPage(): ReactNode {
  const { t, i18n } = useTranslation()

  async function handleGenerateToken() {
    try {
      await AccountApi.generateVerificationCode(i18n.language)
      addToast({
        title: t('account.verification.resend.success'),
        timeout: 2500,
        color: 'success'
      })
    } catch (e) {
      const error = (e as RestError).errorDto
      addToast({
        title: t('account.verification.resend.error'),
        description: error?.message ?? t('error.genericMessage'),
        color: 'danger'
      })
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

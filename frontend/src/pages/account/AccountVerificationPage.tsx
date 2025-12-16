import { ReactNode, use, useEffect, useState } from 'react'
import { centerSection } from '@/styles/primitives.ts'
import { Spinner } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import { useTranslation } from 'react-i18next'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AccountApi from '@/api/AccountApi.ts'
import { AccountVerificationRequestDto } from '@/types/AccountVerificationRequestDto.ts'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast, addSuccessToast } from '@/helpers/ToastHelper.ts'

export function AccountVerificationPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const [queryParams] = useSearchParams()
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const { handleUserUpdate } = use(AuthorizationContext)
  const navigate = useNavigate()

  useEffect(() => {
    async function verifyAccount() {
      try {
        const accountId = Number.parseInt(queryParams.get('id') ?? '')
        const token = queryParams.get('token')

        if (accountId == null || token == null) {
          return
        }

        const request: AccountVerificationRequestDto = {
          id: accountId,
          token
        }

        await AccountApi.verifyAccount(request, i18n.language)
        addSuccessToast(t('account.verification.success'))
        handleUserUpdate()
        navigate(getRoutePath(RouteId.Dashboard))
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('account.verification.error'), error?.message ?? t('error.genericMessage'))
      } finally {
        setIsLoading(false)
      }
    }
    verifyAccount()
  }, [])

  return (
    <section className={centerSection()}>
      {isLoading ? <Spinner /> : <Empty headline={t('account.verification.error')} />}
    </section>
  )
}

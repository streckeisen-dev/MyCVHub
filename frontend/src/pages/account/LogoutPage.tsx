import React, { use, useEffect } from 'react'
import { Empty } from '@/components/Empty.tsx'
import { useTranslation } from 'react-i18next'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import AccountApi from '@/api/AccountApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export function LogoutPage(): React.ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { user, handleLogout } = use(AuthorizationContext)

  useEffect(() => {
    async function logout() {
      if (!user) {
        navigate(getRoutePath(RouteId.Home))
        return
      }
      try {
        await AccountApi.logout(i18n.language)
        handleLogout()
        navigate(getRoutePath(RouteId.Home))
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('account.logout.error'), error?.message ?? t('error.genericMessage'))
        // eslint-disable-next-line @eslint-react/web-api/no-leaked-timeout
        setTimeout(() => {
          navigate(-1)
        }, 2000)
      }
    }
    logout()
  }, [])

  return (
    <Empty
      headline={t('account.logout.headline')}
      title={t('account.logout.action')}
      text={t('account.logout.message')}
    />
  )
}

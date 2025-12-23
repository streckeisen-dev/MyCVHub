import { ReactNode, useEffect } from 'react'
import { Spinner } from '@heroui/react'
import { useNavigate } from 'react-router-dom'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { useTranslation } from 'react-i18next'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'

export function OAuthFailurePage(): ReactNode {
  const { t } = useTranslation()
  const navigate = useNavigate()

  useEffect(() => {
    addErrorToast(t('account.login.oauth.error'))
    navigate(getRoutePath(RouteId.Login))
  })

  return <Spinner />
}

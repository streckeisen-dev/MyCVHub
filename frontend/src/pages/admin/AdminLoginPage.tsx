import AdminAuthApi from '@/api/AdminAuthApi.ts'
import { Button } from '@/components/ui/Button.tsx'
import { Input } from '@/components/ui/Fields.tsx'
import { PageTitle } from '@/components/ui/Layout.tsx'
import { AdminAuthorizationContext } from '@/context/AdminAuthorizationContext.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { Form } from '@heroui/react'
import { FormEvent, ReactNode, use, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'

export function AdminLoginPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const { admin, handleAdminUpdate } = use(AdminAuthorizationContext)
  const [isLoggingIn, setIsLoggingIn] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    if (!admin) return
    navigate(admin.mustChangePassword ? '/admin/change-password' : '/admin', { replace: true })
  }, [admin, navigate])

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsLoggingIn(true)

    const data = Object.fromEntries(new FormData(e.currentTarget))
    try {
      await AdminAuthApi.login(data.username as string, data.password as string, i18n.language)
      await handleAdminUpdate()
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(t('admin.login.error'), error?.message ?? t('error.genericMessage'))
    } finally {
      setIsLoggingIn(false)
    }
  }

  if (admin && !admin.mustChangePassword) {
    const from = typeof location.state?.from === 'string' ? location.state.from : '/admin'
    return <Navigate to={from} replace />
  }

  return (
    <section className="mx-auto flex min-h-[calc(100vh-10rem)] w-full max-w-sm flex-col items-center justify-center gap-7 px-4 py-10">
      <div className="text-center">
        <PageTitle>{t('admin.login.title')}</PageTitle>
      </div>

      <Form className="flex w-full flex-col gap-4" onSubmit={handleSubmit}>
        <Input isRequired label={t('fields.username')} name="username" type="text" />
        <Input isRequired label={t('fields.password')} name="password" type="password" />

        <Button type="submit" variant="primary" className="mt-1 w-full" isPending={isLoggingIn}>
          {t('admin.login.action')}
        </Button>
      </Form>
    </section>
  )
}

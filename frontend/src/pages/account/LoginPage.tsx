import { Button } from '@/components/ui/Button.tsx'
import { Input } from '@/components/ui/Fields.tsx'
import { FormEvent, JSX, use, useEffect, useState } from 'react'
import { Form } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import AccountApi from '@/api/AccountApi.ts'
import { RestError } from '@/types/RestError.ts'
import { FaGithub } from 'react-icons/fa'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { PageTitle } from '@/components/ui/Layout.tsx'

export default function LoginPage(): JSX.Element {
  const { t, i18n } = useTranslation()
  const { user, handleUserUpdate } = use(AuthorizationContext)
  const navigate = useNavigate()
  const params = useParams()

  useEffect(() => {
    if (user) {
      navigate(getRoutePath(RouteId.Dashboard))
    }
  }, [user])

  const [isLoggingIn, setIsLoggingIn] = useState(false)

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsLoggingIn(true)

    const data = Object.fromEntries(new FormData(e.currentTarget))

    try {
      await AccountApi.login(data.username as string, data.password as string, i18n.language)
      handleUserUpdate()
    } catch (e) {
      const error = (e as RestError).errorDto
      addErrorToast(t('account.login.error'), error?.message ?? t('error.genericMessage'))
    } finally {
      setIsLoggingIn(false)
    }
  }

  function loginWith(provider: string) {
    const redirect = params.redirect ? decodeURIComponent(params.redirect) : ''
    globalThis.location.href = `/api/auth/oauth2/authorization/${provider}${redirect}`
  }

  return (
    <section className="mx-auto flex min-h-[calc(100vh-15rem)] w-full max-w-sm flex-col items-center justify-center gap-7 px-4 py-10">
      <div className="text-center">
        <PageTitle>Login</PageTitle>
      </div>

      <Form className="flex w-full flex-col gap-4" onSubmit={handleSubmit}>
        <Input isRequired label={t('fields.username')} name="username" type="text" />
        <Input isRequired label={t('fields.password')} name="password" type="password" />

        <Button type="submit" variant="primary" className="mt-1 w-full" isPending={isLoggingIn}>
          {t('account.login.action')}
        </Button>

        <Button
          className="w-full"
          onPress={() => loginWith('github')}
        >
          <FaGithub size={25} />
          {t('account.login.oauth.github')}
        </Button>
      </Form>
      <p className="flex flex-wrap items-center justify-center gap-x-2 gap-y-1 text-sm text-default-600">
        <span>{t('account.login.noAccount')}</span>
        <Button
          className="min-h-0 px-1 py-0 text-sm"
          variant="tertiary"
          as={Link}
          to={getRoutePath(RouteId.Signup)}
        >
          {t('account.login.signup')}
        </Button>
      </p>
    </section>
  )
}

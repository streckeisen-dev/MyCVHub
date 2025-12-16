import { FormEvent, JSX, use, useEffect, useState } from 'react'
import { title } from '@/styles/primitives.ts'
import { Button, Form, Input } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import AccountApi from '@/api/AccountApi.ts'
import { RestError } from '@/types/RestError.ts'
import { FaGithub } from 'react-icons/fa'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

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
    <section className="flex flex-col items-center justify-center gap-5 py-8 md:py-10">
      <div className="inline-block max-w-lg text-center justify-center">
        <span className={title()}>Login</span>
      </div>

      <Form className="gap-5 w-full md:w-1/2 xl:w-1/3" onSubmit={handleSubmit}>
        <Input
          isRequired
          label={t('fields.username')}
          labelPlacement="inside"
          name="username"
          type="text"
        />
        <Input
          isRequired
          label={t('fields.password')}
          labelPlacement="inside"
          name="password"
          type="password"
        />

        <Button type="submit" color="primary" className="w-full" isLoading={isLoggingIn}>
          {t('account.login.action')}
        </Button>

        <Button
          className="w-full"
          startContent={<FaGithub size={25} />}
          onPress={() => loginWith('github')}
        >
          {t('account.login.oauth.github')}
        </Button>
      </Form>
      <p>
        {t('account.login.noAccount')}{' '}
        <Button
          className="text-medium"
          variant="light"
          color="primary"
          as={Link}
          to={getRoutePath(RouteId.Signup)}
        >
          {t('account.login.signup')}
        </Button>
      </p>
    </section>
  )
}

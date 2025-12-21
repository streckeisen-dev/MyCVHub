import { FormEvent, ReactNode, use, useEffect, useState } from 'react'
import { centerSection, h1, twoColumnForm } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { AccountEditorData } from '@/types/account/AccountEditorData.ts'
import { PasswordForm, PasswordFormState } from '@/components/account/PasswordForm.tsx'
import { AccountForm } from '@/components/account/AccountForm.tsx'
import { Form } from '@heroui/react'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { FormButtons } from '@/components/FormButtons.tsx'
import { PasswordRequirements } from '@/components/account/PasswordRequirements.tsx'
import { toDateString } from '@/helpers/DateHelper.ts'
import AccountApi from '@/api/AccountApi.ts'
import { SignupRequestDto } from '@/types/account/SignUpRequestDto.ts'
import { RestError } from '@/types/RestError.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'

export function SignupPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [accountFormState, setAccountFormState] = useState<AccountEditorData>({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    birthday: null,
    language: i18n.language,
    street: '',
    houseNumber: '',
    postcode: '',
    city: '',
    country: 'CH'
  })
  const [passwordFormState, setPasswordFormState] = useState<PasswordFormState>({
    password: '',
    confirmPassword: ''
  })
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  const { user, handleUserUpdate } = use(AuthorizationContext)

  useEffect(() => {
    if (user) {
      navigate(getRoutePath(RouteId.Dashboard))
    }
  }, [])

  function handleAccountChange(name: string, value: unknown) {
    setAccountFormState((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
    setErrorMessages((prev: ErrorMessages) => {
      return {
        ...prev,
        [name]: undefined
      } as ErrorMessages
    })
  }

  function handlePasswordChange(name: string, value: string | undefined) {
    setPasswordFormState((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
    setErrorMessages((prev: ErrorMessages) => {
      return {
        ...prev,
        [name]: undefined
      } as ErrorMessages
    })
  }

  function handleCancel() {
    navigate(getRoutePath(RouteId.Home))
  }

  async function handleSave(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)

    const request: SignupRequestDto = {
      ...accountFormState,
      houseNumber: accountFormState.houseNumber === '' ? undefined : accountFormState.houseNumber,
      birthday: toDateString(accountFormState.birthday),
      ...passwordFormState
    }

    try {
      await AccountApi.signUp(request, i18n.language)
      handleUserUpdate()
      navigate(getRoutePath(RouteId.Dashboard))
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('account.signup.error'), setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className={centerSection()}>
      <h1 className={h1()}>{t('account.create.title')}</h1>

      <Form className={twoColumnForm()} onSubmit={handleSave}>
        <AccountForm
          state={accountFormState}
          onChange={handleAccountChange}
          errorMessages={errorMessages}
        />

        <div className="flex flex-col gap-6">
          <PasswordForm
            state={passwordFormState}
            onChange={handlePasswordChange}
            errorMessages={errorMessages}
          />
        </div>
        <PasswordRequirements state={passwordFormState} />
        <FormButtons onCancel={handleCancel} isSaving={isSaving} />
      </Form>
    </section>
  )
}

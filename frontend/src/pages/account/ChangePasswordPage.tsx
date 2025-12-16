import { FormEvent, ReactNode, useState } from 'react'
import { centerSection, title, twoColumnForm } from '@/styles/primitives.ts'
import { PasswordForm, PasswordFormState, PasswordInput } from '@/components/account/PasswordForm.tsx'
import { useTranslation } from 'react-i18next'
import { Form } from '@heroui/react'
import { PasswordRequirements } from '@/components/account/PasswordRequirements.tsx'
import { FormButtons } from '@/components/FormButtons.tsx'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import AccountApi from '@/api/AccountApi.ts'
import { RestError } from '@/types/RestError.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { addSuccessToast } from '@/helpers/ToastHelper.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'

export function ChangePasswordPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [oldPassword, setOldPassword] = useState<string | undefined>('')
  const [passwordFormState, setPasswordFormState] = useState<PasswordFormState>({
    password: '',
    confirmPassword: ''
  })
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleChange(name: string, value: string | undefined) {
    setPasswordFormState((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
  }

  function handleCancel() {
    navigate(getRoutePath(RouteId.Account))
  }

  async function handleSave(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)

    try {
      await AccountApi.changePassword(
        {
          oldPassword: oldPassword,
          password: passwordFormState.password,
          confirmPassword: passwordFormState.confirmPassword
        },
        i18n.language
      )
      addSuccessToast(t('account.changePassword.success'))
      navigate(getRoutePath(RouteId.Account))
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('account.changePassword.error'), setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className={centerSection()}>
      <h1 className={title()}>{t('account.edit.changePassword')}</h1>
      <Form className={twoColumnForm()} onSubmit={handleSave}>
        <div className="flex flex-col gap-6">
          <PasswordInput
            name="oldPassword"
            isRequired
            label={t('fields.oldPassword')}
            value={oldPassword}
            errorMessage={errorMessages.oldPassword}
            onChange={(e) => setOldPassword(e.currentTarget.value)}
          />
          <PasswordForm
            state={passwordFormState}
            onChange={handleChange}
            errorMessages={errorMessages}
          />
        </div>
        <PasswordRequirements state={passwordFormState} />
        <FormButtons onCancel={handleCancel} isSaving={isSaving} />
      </Form>
    </section>
  )
}

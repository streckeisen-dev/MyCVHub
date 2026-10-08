import AdminAuthApi from '@/api/AdminAuthApi.ts'
import { PasswordForm, PasswordFormState } from '@/components/account/PasswordForm.tsx'
import { PasswordRequirements } from '@/components/account/PasswordRequirements.tsx'
import { FormButtons } from '@/components/btn/FormButtons.tsx'
import { PageTitle } from '@/components/ui/Layout.tsx'
import { AdminAuthorizationContext } from '@/context/AdminAuthorizationContext.tsx'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { addSuccessToast } from '@/helpers/ToastHelper.ts'
import { centerSection, twoColumnForm } from '@/styles/primitives.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { RestError } from '@/types/RestError.ts'
import { Form } from '@heroui/react'
import { FormEvent, ReactNode, use, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'

export function AdminChangePasswordPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const { handleAdminUpdate, handleAdminLogout } = use(AdminAuthorizationContext)
  const navigate = useNavigate()
  const [passwordFormState, setPasswordFormState] = useState<PasswordFormState>({
    password: '',
    confirmPassword: ''
  })
  const [isSaving, setIsSaving] = useState(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleChange(name: string, value: string | undefined) {
    setPasswordFormState((prev) => ({
      ...prev,
      [name]: value
    }))
  }

  async function handleSave(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)

    try {
      await AdminAuthApi.changePassword(
        passwordFormState.password,
        passwordFormState.confirmPassword,
        i18n.language
      )
      addSuccessToast(t('admin.changePassword.success'))
      await handleAdminUpdate()
      navigate('/admin', { replace: true })
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('admin.changePassword.error'), setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  async function handleCancel() {
    await AdminAuthApi.logout(i18n.language)
    handleAdminLogout()
    navigate('/admin/login', { replace: true })
  }

  return (
    <section className={centerSection()}>
      <PageTitle>{t('admin.changePassword.title')}</PageTitle>
      <Form className={twoColumnForm()} onSubmit={handleSave}>
        <div className="flex flex-col gap-6">
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

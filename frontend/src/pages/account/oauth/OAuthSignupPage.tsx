import { FormEvent, ReactNode, use, useState } from 'react'
import { centerSection, title, twoColumnForm } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { addToast, Form } from '@heroui/react'
import { AccountForm } from '@/components/AccountForm.tsx'
import { AccountEditorData } from '@/types/AccountEditorData.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { FormButtons } from '@/components/FormButtons.tsx'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { RestError } from '@/types/RestError.ts'
import AccountApi from '@/api/AccountApi.ts'
import { OAuthSignUpRequestDto } from '@/types/OAuthSignUpRequestDto.ts'
import { toDateString } from '@/helpers/DateHelper.ts'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'

export function OAuthSignupPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { handleUserUpdate } = use(AuthorizationContext)

  const [accountData, setAccountData] = useState<AccountEditorData>({
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
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})
  const [isSaving, setIsSaving] = useState<boolean>(false)

  function handleDataChange(name: string, value: unknown) {
    setAccountData((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
  }

  async function handleCancel() {
    try {
      await AccountApi.deleteAccount(i18n.language)
      await AccountApi.logout(i18n.language)
      navigate(getRoutePath(RouteId.Home))
    } catch (e) {
      const error = (e as RestError).errorDto
      addToast({
        title: t('account.accountMgmt.delete.error'),
        description: error?.message ?? t('error.genericMessage'),
        color: 'danger'
      })
    }
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    setIsSaving(true)
    const request: OAuthSignUpRequestDto = {
      ...accountData,
      houseNumber: accountData.houseNumber === '' ? undefined : accountData.houseNumber,
      birthday: toDateString(accountData.birthday)
    }
    try {
      await AccountApi.oauthSignUp(request, i18n.language)
      handleUserUpdate()
      navigate(getRoutePath(RouteId.Dashboard))
    } catch (e) {
      const error = (e as RestError).errorDto
      const messages = error?.errors ?? {}
      setErrorMessages(messages)
      if (Object.keys(messages).length === 0) {
        addToast({
          title: t('account.signup.error'),
          description: error?.message ?? t('error.genericMessage'),
          color: 'danger'
        })
      }
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className={centerSection()}>
      <h1 className={title()}>{t('account.create.title')}</h1>
      <Form className={twoColumnForm()} onSubmit={handleSubmit}>
        <AccountForm
          state={accountData}
          onChange={handleDataChange}
          errorMessages={errorMessages}
        />
        <FormButtons onCancel={handleCancel} isSaving={isSaving} />
      </Form>
    </section>
  )
}

import React, { FormEvent, use, useEffect, useState } from 'react'
import { centerSection, title, twoColumnForm } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import AccountApi from '@/api/AccountApi.ts'
import { addToast, Form, Spinner } from '@heroui/react'
import { Empty } from '@/components/Empty.tsx'
import { AccountForm, toAccountEditorData } from '@/components/AccountForm.tsx'
import { toDateString } from '@/helpers/DateHelper.ts'
import { useNavigate } from 'react-router-dom'
import { AccountEditorData } from '@/types/AccountEditorData.ts'
import { AccountUpdateDto } from '@/types/AccountUpdateDto.ts'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { RestError } from '@/types/RestError.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { FormButtons } from '@/components/FormButtons.tsx'

export function EditAccountPage(): React.ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { handleUserUpdate } = use(AuthorizationContext)

  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [accountEditorData, setAccountEditorData] = useState<AccountEditorData>()
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  useEffect(() => {
    async function loadAccount() {
      try {
        const result = await AccountApi.getAccount(i18n.language)
        setAccountEditorData(toAccountEditorData(result))
      } finally {
        setIsLoading(false)
      }
    }
    loadAccount()
  }, [])

  async function handleSave(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (!accountEditorData) {
      return
    }
    setIsSaving(true)

    const updateRequest: AccountUpdateDto = {
      ...accountEditorData,
      houseNumber: accountEditorData.houseNumber === '' ? undefined : accountEditorData.houseNumber,
      birthday: toDateString(accountEditorData.birthday)
    }

    try {
      await AccountApi.updateAccount(updateRequest, i18n.language)
      handleUserUpdate()
      navigate(getRoutePath(RouteId.Account))
    } catch (e) {
      const error = (e as RestError).errorDto
      const messages = error?.errors ?? {}
      setErrorMessages(messages)
      if (Object.keys(messages).length === 0) {
        addToast({
          title: t('account.edit.error'),
          description: error?.message ?? t('error.genericMessage'),
          color: 'danger'
        })
      }
    } finally {
      setIsSaving(false)
    }
  }

  function handleChange(name: string, value: unknown) {
    setAccountEditorData((prev) => {
      return {
        ...prev,
        [name]: value
      } as AccountEditorData
    })
  }

  function handleCancel() {
    navigate(getRoutePath(RouteId.Account))
  }

  const content = accountEditorData ? (
    <>
      <h1 className={title()}>{t('account.edit.title')}</h1>
      <Form className={twoColumnForm()} onSubmit={handleSave}>
        <AccountForm
          state={accountEditorData}
          onChange={handleChange}
          errorMessages={errorMessages}
        />
        <FormButtons onCancel={handleCancel} isSaving={isSaving} />
      </Form>
    </>
  ) : (
    <Empty headline={t('account.loadingError.title')} title={t('account.loadingError.text')} />
  )

  return <section className={centerSection()}>{isLoading ? <Spinner /> : content}</section>
}

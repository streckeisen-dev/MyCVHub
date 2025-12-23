import { FormEvent, ReactNode, use, useState } from 'react'
import { centerSection, title, twoColumnForm } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { Form } from '@heroui/react'
import { AccountForm } from '@/components/account/AccountForm.tsx'
import { AccountEditorData } from '@/types/account/AccountEditorData.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { FormButtons } from '@/components/FormButtons.tsx'
import { Link, useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { RestError } from '@/types/RestError.ts'
import AccountApi from '@/api/AccountApi.ts'
import { OAuthSignUpRequestDto } from '@/types/account/OAuthSignUpRequestDto.ts'
import { toDateString } from '@/helpers/DateHelper.ts'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'

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
  const [acceptedTos, setAcceptedTos] = useState<boolean>(false)
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
      addErrorToast(
        t('account.accountMgmt.delete.error'),
        error?.message ?? t('error.genericMessage')
      )
    }
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    setIsSaving(true)
    const request: OAuthSignUpRequestDto = {
      ...accountData,
      houseNumber: accountData.houseNumber === '' ? undefined : accountData.houseNumber,
      birthday: toDateString(accountData.birthday),
      acceptsTos: acceptedTos
    }
    try {
      await AccountApi.oauthSignUp(request, i18n.language)
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
      <h1 className={title()}>{t('account.create.title')}</h1>
      <Form className={twoColumnForm()} onSubmit={handleSubmit}>
        <AccountForm
          state={accountData}
          onChange={handleDataChange}
          errorMessages={errorMessages}
        />

        <div className="col-span-1 sm:col-span-2">
          <CheckboxInput
            name="acceptsTos"
            label={
              <p>
                {t('fields.acceptTos')}
                <Link
                  className="text-primary"
                  to={getRoutePath(RouteId.TermsOfService)}
                  target="_blank"
                >
                  {t('tos.title')}
                </Link>
                {', '}
                <Link
                  className="text-primary"
                  to={getRoutePath(RouteId.PrivacyPolicy)}
                  target="_blank"
                >
                  {t('privacy.title')}
                </Link>
              </p>
            }
            isSelected={acceptedTos}
            onValueChange={(val) => setAcceptedTos(val)}
            isInvalid={errorMessages.acceptsTos != null}
            errorMessage={errorMessages.acceptsTos}
          />
        </div>
        <FormButtons onCancel={handleCancel} isSaving={isSaving} />
      </Form>
    </section>
  )
}

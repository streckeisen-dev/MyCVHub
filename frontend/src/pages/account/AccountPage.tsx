import React, { PropsWithChildren, useEffect, useState } from 'react'
import { centerSection, h2, h3 } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { AccountDto } from '@/types/AccountDto.ts'
import { Button, Card, CardBody, CardFooter, CardHeader, CircularProgress } from '@heroui/react'
import AccountApi from '@/api/AccountApi.ts'
import { Empty } from '@/components/Empty.tsx'
import { Attribute, AttributeList } from '@/components/AttributeList.tsx'
import { TFunction } from 'i18next'
import { FaEdit } from 'react-icons/fa'
import { AccountDeleteButton } from '@/components/account/AccountDeleteButton.tsx'
import { Link } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { formatDate } from '@/helpers/DateHelper.ts'

function getPersonalDataAttributes(t: TFunction, lang: string, account: AccountDto): Attribute[] {
  return [
    {
      name: t('fields.firstName'),
      value: account.firstName
    },
    {
      name: t('fields.lastName'),
      value: account.lastName
    },
    {
      name: t('fields.email'),
      value: account.email
    },
    {
      name: t('fields.phone'),
      value: account.phone
    },
    {
      name: t('fields.birthday'),
      value: formatDate(account.birthday, lang)
    }
  ]
}

function getAddressAttributes(t: TFunction, account: AccountDto): Attribute[] {
  return [
    {
      name: t('fields.street'),
      value: account.street
    },
    {
      name: t('fields.houseNumber'),
      value: account.houseNumber ?? '―'
    },
    {
      name: t('fields.postcode'),
      value: account.postcode
    },
    {
      name: t('fields.city'),
      value: account.city
    },
    {
      name: t('fields.country'),
      value: account.country
    }
  ]
}

function getSecurityAttributes(t: TFunction, account: AccountDto): Attribute[] {
  const authTypes: string[] = []
  if (account.hasPassword) {
    authTypes.push(t('account.security.userPw'))
  }
  if (account.oauthIntegrations.length > 0) {
    authTypes.push(t('account.security.oauthMethod'))
  }
  const attributes: Attribute[] = [
    {
      name: t('account.security.authType'),
      value: authTypes.join(', ')
    }
  ]
  if (account.oauthIntegrations.length > 0) {
    attributes.push({
      name: t('account.security.oauth'),
      value: account.oauthIntegrations
        .map((integration) => integration.provider)
        .map((provider) => (provider === 'GITHUB' ? 'GitHub' : provider))
        .join(', ')
    })
  }
  return attributes
}

type AccountTileProps = PropsWithChildren & {
  title: string
  action?: React.ReactNode
}

function AccountTile(props: AccountTileProps): React.ReactNode {
  const { title, action, children } = props
  return (
    <Card className="w-full p-3 md:p-5 lg:p-10">
      <CardHeader>
        <h3 className={h3()}>{title}</h3>
      </CardHeader>
      <CardBody>{children}</CardBody>
      {action && <CardFooter>{action}</CardFooter>}
    </Card>
  )
}

export function AccountPage(): React.ReactNode {
  const { t, i18n } = useTranslation()
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [account, setAccount] = useState<AccountDto>()

  useEffect(() => {
    async function loadAccount() {
      try {
        const result = await AccountApi.getAccount(i18n.language)
        setAccount(result)
      } finally {
        setIsLoading(false)
      }
    }
    loadAccount()
  }, [])

  const content = account ? (
    <>
      <h2 className={h2()}>{t('account.title')}</h2>
      <Button
        className="self-end"
        color="primary"
        startContent={<FaEdit />}
        as={Link}
        to={getRoutePath(RouteId.EditAccount)}
      >
        {t('account.edit.title')}
      </Button>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5 w-full">
        <AccountTile title={t('account.personalData')}>
          <AttributeList attributes={getPersonalDataAttributes(t, i18n.language, account)} />
        </AccountTile>
        <AccountTile title={t('account.address')}>
          <AttributeList attributes={getAddressAttributes(t, account)} />
        </AccountTile>
        <AccountTile
          title={t('account.security.title')}
          action={
            account.hasPassword && (
              <Button
                className="w-min self-end"
                color="primary"
                as={Link}
                to={getRoutePath(RouteId.ChangePassword)}
              >
                {t('account.edit.changePassword')}
              </Button>
            )
          }
        >
          <AttributeList attributes={getSecurityAttributes(t, account)} />
        </AccountTile>
        <AccountTile title={t('account.accountMgmt.title')} action={<AccountDeleteButton />} />
      </div>
    </>
  ) : (
    <Empty headline={t('account.loadingError.title')} title={t('account.loadingError.text')} />
  )

  return <section className={centerSection()}>{isLoading ? <CircularProgress /> : content}</section>
}

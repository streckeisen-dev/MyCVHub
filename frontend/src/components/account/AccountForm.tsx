import React, { ChangeEvent, ReactNode, useEffect, useState } from 'react'
import {
  Autocomplete,
  AutocompleteItem,
  AutocompleteProps,
  DatePicker,
  DateValue,
  Input
} from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { h2 } from '@/styles/primitives.ts'
import { CountryDto } from '@/types/CountryDto.ts'
import CountryApi from '@/api/CountryApi.ts'
import { AccountEditorData } from '@/types/AccountEditorData.ts'
import { Key } from '@react-types/shared'
import { SUPPOERTED_LANGUAGES } from '@/config/Languages.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { AccountDto } from '@/types/AccountDto.ts'
import { stringToCalendarDate } from '@/helpers/DateHelper.ts'
import { getLocalTimeZone, today } from '@internationalized/date'
import { addErrorToast } from '@/helpers/ToastHelper.ts'

export function toAccountEditorData(account: AccountDto): AccountEditorData {
  return {
    username: account.username,
    firstName: account.firstName,
    lastName: account.lastName,
    email: account.email,
    phone: account.phone,
    birthday: stringToCalendarDate(account.birthday),
    street: account.street,
    houseNumber: account.houseNumber,
    postcode: account.postcode,
    city: account.city,
    country: account.country,
    language: account.language
  }
}

function CountrySelect(props: Readonly<Omit<AutocompleteProps, 'children'>>): ReactNode {
  const { t, i18n } = useTranslation()
  const [countries, setCountries] = useState<CountryDto[]>([])

  useEffect(() => {
    async function loadCountries() {
      try {
        const countries = await CountryApi.getCountries(i18n.language)
        setCountries(countries)
      } catch (_e) {
        addErrorToast(t('country.loadingError.title'), t('country.loadingError.message'))
      }
    }
    loadCountries()
  }, [])

  return (
    <Autocomplete label={t('fields.country')} {...props}>
      {countries.map((country) => (
        <AutocompleteItem key={country.countryCode}>{country.name}</AutocompleteItem>
      ))}
    </Autocomplete>
  )
}

export type AccountFormProps = Readonly<{
  state: AccountEditorData
  onChange: (name: string, value: unknown) => void
  errorMessages: ErrorMessages
}>

export function AccountForm(props: AccountFormProps): React.ReactNode {
  const { t } = useTranslation()
  const { state, errorMessages, onChange } = props

  function handleFormChange(e: ChangeEvent<HTMLInputElement>) {
    onChange(e.target.name, e.target.value)
  }

  function handleBirthdayChange(date: DateValue | null) {
    onChange('birthday', date ?? undefined)
  }

  function handleLanguageChange(langKey: Key | null) {
    onChange('language', langKey ? (langKey as string) : undefined)
  }

  function handleCountryChange(countryKey: Key | null) {
    onChange('country', countryKey ? (countryKey as string) : undefined)
  }

  return (
    <>
      <div className="flex flex-col gap-6">
        <h2 className={h2()}>{t('account.personalData')}</h2>

        <Input
          type="text"
          name="username"
          label={t('fields.username')}
          value={state.username}
          onChange={handleFormChange}
          isInvalid={errorMessages.username != null}
          errorMessage={errorMessages.username}
          isRequired
        />
        <Input
          type="text"
          name="firstName"
          label={t('fields.firstName')}
          value={state.firstName}
          onChange={handleFormChange}
          isInvalid={errorMessages.firstName != null}
          errorMessage={errorMessages.firstName}
          isRequired
        />
        <Input
          type="text"
          name="lastName"
          label={t('fields.lastName')}
          value={state.lastName}
          onChange={handleFormChange}
          isInvalid={errorMessages.lastName != null}
          errorMessage={errorMessages.lastName}
          isRequired
        />
        <Input
          type="email"
          name="email"
          label={t('fields.email')}
          isRequired
          value={state.email}
          isInvalid={errorMessages.email != null}
          errorMessage={errorMessages.email}
          onChange={handleFormChange}
        />
        <Input
          type="text"
          name="phone"
          label={t('fields.phone')}
          isRequired
          value={state.phone}
          onChange={handleFormChange}
          isInvalid={errorMessages.phone != null}
          errorMessage={errorMessages.phone}
        />
        <DatePicker
          name="birthday"
          label={t('fields.birthday')}
          isRequired
          maxValue={today(getLocalTimeZone())}
          value={state.birthday}
          onChange={handleBirthdayChange}
          showMonthAndYearPickers={true}
          isInvalid={errorMessages.birthday != null}
          errorMessage={errorMessages.birthday}
        />

        <Autocomplete
          label={t('fields.language')}
          name="language"
          isRequired
          selectedKey={state.language}
          onSelectionChange={handleLanguageChange}
          isInvalid={errorMessages.language != null}
          errorMessage={errorMessages.language}
        >
          {SUPPOERTED_LANGUAGES.map((lang) => (
            <AutocompleteItem key={lang.key}>{lang.name}</AutocompleteItem>
          ))}
        </Autocomplete>
      </div>

      <div className="flex flex-col gap-6">
        <h2 className={h2()}>{t('account.address')}</h2>

        <Input
          type="text"
          name="street"
          label={t('fields.street')}
          value={state.street}
          onChange={handleFormChange}
          isInvalid={errorMessages.street != null}
          errorMessage={errorMessages.street}
          isRequired
        />
        <Input
          type="text"
          name="houseNumber"
          label={t('fields.houseNumber')}
          value={state.houseNumber}
          onChange={handleFormChange}
          isInvalid={errorMessages.houseNumber != null}
          errorMessage={errorMessages.houseNumber}
        />
        <Input
          type="text"
          name="postcode"
          label={t('fields.postcode')}
          value={state.postcode}
          onChange={handleFormChange}
          isInvalid={errorMessages.postcode != null}
          errorMessage={errorMessages.postcode}
          isRequired
        />
        <Input
          type="text"
          name="city"
          label={t('fields.city')}
          isRequired
          value={state.city}
          onChange={handleFormChange}
          isInvalid={errorMessages.city != null}
          errorMessage={errorMessages.city}
        />
        <CountrySelect
          selectedKey={state.country}
          onSelectionChange={handleCountryChange}
          name="country"
          isRequired
          isInvalid={errorMessages.country != null}
          errorMessage={errorMessages.country}
        />
      </div>
    </>
  )
}

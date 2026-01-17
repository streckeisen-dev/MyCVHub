import { ReactNode } from 'react'
import { Autocomplete, AutocompleteItem, AutocompleteProps } from '@heroui/react'
import { SUPPOERTED_LANGUAGES } from '@/config/Languages.ts'
import { useTranslation } from 'react-i18next'

export type LanguageInputProps = Omit<AutocompleteProps, 'children'>

export function LanguageInput(props: LanguageInputProps): ReactNode {
  const { t } = useTranslation()

  return (
    <Autocomplete label={t('fields.language')} name="language" isRequired {...props}>
      {SUPPOERTED_LANGUAGES.map((lang) => (
        <AutocompleteItem key={lang.key}>{lang.name}</AutocompleteItem>
      ))}
    </Autocomplete>
  )
}

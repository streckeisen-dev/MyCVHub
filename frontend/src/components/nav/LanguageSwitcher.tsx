import React from 'react'
import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { SUPPOERTED_LANGUAGES } from '@/config/Languages.ts'

export type LanguageSwitcherProps = Readonly<{
  className?: string
}>

export function LanguageSwitcher(props: LanguageSwitcherProps): React.ReactNode {
  const {i18n} = useTranslation()
  const { className } = props

  function changeLanguage(lang: string) {
    i18n.changeLanguage(lang)
  }

  return (
    <Dropdown>
      <DropdownTrigger>
        <Button variant="bordered" className={className}>
          {SUPPOERTED_LANGUAGES.find(lang => lang.key === i18n.language)?.name ?? i18n.language}
        </Button>
      </DropdownTrigger>
      <DropdownMenu>
        {SUPPOERTED_LANGUAGES.map(lang =>
          <DropdownItem key={lang.key} onClick={() => changeLanguage(lang.key)}>{lang.name}</DropdownItem>
        )}
      </DropdownMenu>
    </Dropdown>
  )
}
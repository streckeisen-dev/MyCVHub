import React from 'react'

import { useTranslation } from 'react-i18next'
import { SUPPOERTED_LANGUAGES } from '@/config/Languages.ts'
import { Dropdown, Label } from '@heroui/react'
import { DropdownButton } from '@/components/ui/DropdownButton.tsx'

export type LanguageSwitcherProps = Readonly<{
  className?: string
}>

export function LanguageSwitcher(props: LanguageSwitcherProps): React.ReactNode {
  const { i18n } = useTranslation()
  const { className } = props

  function changeLanguage(lang: string) {
    i18n.changeLanguage(lang)
  }

  return (
    <Dropdown>
      <DropdownButton className={className}>
        <span>
          {SUPPOERTED_LANGUAGES.find((lang) => lang.key === i18n.language)?.name ?? i18n.language}
        </span>
      </DropdownButton>
      <Dropdown.Popover>
        <Dropdown.Menu aria-label="Language selection">
          {SUPPOERTED_LANGUAGES.map((lang) => (
            <Dropdown.Item
              key={lang.key}
              id={lang.key}
              textValue={lang.name}
              onAction={() => changeLanguage(lang.key)}
            >
              <Label>{lang.name}</Label>
            </Dropdown.Item>
          ))}
        </Dropdown.Menu>
      </Dropdown.Popover>
    </Dropdown>
  )
}

import { Button } from '@/components/ui/Button.tsx'

import DefaultAvatar from '@/assets/default_profile_picture_thumbnail.png'
import { Key, ReactNode, use } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId, SITE_CONFIG } from '@/config/RouteTree.tsx'
import { Dropdown, Label } from '@heroui/react'

type OverlayPlacement =
  | 'top'
  | 'bottom'
  | 'right'
  | 'left'
  | 'top-start'
  | 'top-end'
  | 'bottom-start'
  | 'bottom-end'
  | 'left-start'
  | 'left-end'
  | 'right-start'
  | 'right-end'
type DropdownPlacement =
  | 'top'
  | 'bottom'
  | 'right'
  | 'left'
  | 'top start'
  | 'top end'
  | 'bottom start'
  | 'bottom end'
  | 'left top'
  | 'left bottom'
  | 'right top'
  | 'right bottom'

interface AccountMenuProps {
  dropdownPlacement?: OverlayPlacement
  onNavigate?: () => void
}

const defaultValue: AccountMenuProps = {
  dropdownPlacement: 'bottom-end',
  onNavigate: undefined
}

function toDropdownPlacement(placement?: OverlayPlacement): DropdownPlacement | undefined {
  switch (placement) {
    case 'left-start':
      return 'left top'
    case 'left-end':
      return 'left bottom'
    case 'right-start':
      return 'right top'
    case 'right-end':
      return 'right bottom'
    default:
      return placement?.replace('-', ' ') as DropdownPlacement | undefined
  }
}

export function AccountMenu(props: AccountMenuProps = defaultValue): ReactNode {
  const { t } = useTranslation()
  const { user } = use(AuthorizationContext)
  const { dropdownPlacement, onNavigate } = props
  const navigate = useNavigate()

  function handleAccountMenuAction(key: Key) {
    const selectedItem = SITE_CONFIG.accountMenu.find((item) => item.label === key)

    if (selectedItem) {
      navigate(selectedItem.href)
      onNavigate?.()
    }
  }

  const loginButton = (
    <Button as={Link} to={getRoutePath(RouteId.Login)} variant="primary" onPress={onNavigate}>
      {t('account.login.action')}
    </Button>
  )

  const accountMenu = SITE_CONFIG.accountMenu.map((item) => (
    <Dropdown.Item key={item.label} id={item.label} textValue={t(item.label)}>
      {item.icon}
      <Label>{t(item.label)}</Label>
    </Dropdown.Item>
  ))

  const menu = (
    <Dropdown>
      <Dropdown.Trigger
        className="inline-flex cursor-pointer items-center gap-2 rounded-sm border-2 border-default p-1"
        type="button"
      >
        <img
          alt=""
          className="h-8 w-8 rounded-full"
          src={user?.thumbnail?.thumbnailUrl ?? DefaultAvatar}
        />
        <span className="text-sm">{user?.displayName}</span>
      </Dropdown.Trigger>
      <Dropdown.Popover placement={toDropdownPlacement(dropdownPlacement)}>
        <Dropdown.Menu aria-label="Profile Actions" onAction={handleAccountMenuAction}>
          {accountMenu}
        </Dropdown.Menu>
      </Dropdown.Popover>
    </Dropdown>
  )

  return user ? menu : loginButton
}

import { Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger, User } from '@heroui/react'

import DefaultAvatar from '@/assets/default_profile_picture_thumbnail.png'
import { OverlayPlacement } from '@heroui/aria-utils'
import { ReactNode, use } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, RouteId, SITE_CONFIG } from '@/config/RouteTree.tsx'

interface AccountMenuProps {
  dropdownPlacement?: OverlayPlacement
  onNavigate?: () => void
}

const defaultValue: AccountMenuProps = {
  dropdownPlacement: 'bottom-end',
  onNavigate: undefined
}

export function AccountMenu(props: AccountMenuProps = defaultValue): ReactNode {
  const { t } = useTranslation()
  const { user } = use(AuthorizationContext)
  const { dropdownPlacement, onNavigate } = props

  const loginButton = (
    <Button as={Link} to={getRoutePath(RouteId.Login)} color="primary" onPress={onNavigate}>
      {t('account.login.action')}
    </Button>
  )

  const accountMenu = SITE_CONFIG.accountMenu.map((item) => (
    <DropdownItem
      key={item.label}
      className="h-14 gap-2"
      as={Link}
      href={item.href}
      startContent={item.icon}
      onPress={onNavigate}
    >
      {t(item.label)}
    </DropdownItem>
  ))

  const menu = (
    <Dropdown placement={dropdownPlacement}>
      <DropdownTrigger>
        <User
          avatarProps={{
            src: user?.thumbnail?.thumbnailUrl ?? DefaultAvatar,
            size: 'sm'
          }}
          name={user?.displayName}
          className="p-1 border-2 border-default cursor-pointer"
        />
      </DropdownTrigger>
      <DropdownMenu aria-label="Profile Actions">{accountMenu}</DropdownMenu>
    </Dropdown>
  )

  return user ? menu : loginButton
}

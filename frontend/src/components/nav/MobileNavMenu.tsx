import { NavbarMenu, NavbarMenuItem, link as linkStyles } from '@/components/ui/Navigation.tsx'
import { Divider } from '@/components/ui/Display.tsx'
import { Fragment, ReactNode, use, useState } from 'react'
import { LanguageSwitcher } from '@/components/nav/LanguageSwitcher.tsx'

import { AccountMenu } from '@/components/nav/AccountMenu.tsx'
import { AuthorizationContext, AuthorizedUser } from '@/context/AuthorizationContext.tsx'
import { NavItemLeaf, NavItemNode, SITE_CONFIG } from '@/config/RouteTree.tsx'
import { NavLink } from 'react-router-dom'
import clsx from 'clsx'
import { useTranslation } from 'react-i18next'

import classes from './Navbar.module.css'
import { TFunction } from 'i18next'
import { FaChevronDown, FaChevronUp } from 'react-icons/fa6'

export type MobileNavMenuProps = Readonly<{
  onLinkClick: () => void
}>

function renderLink(navLink: NavItemLeaf, user: AuthorizedUser | undefined, t: TFunction, onLinkClick: () => void, indent = false): ReactNode {
  return (
    <NavbarMenuItem key={navLink.id}>
      <NavLink
        className={({ isActive }) =>
          clsx(
            linkStyles({ color: 'foreground' }),
            'data-[active=true]:text-primary data-[active=true]:font-medium',
            isActive ? classes.activeLink : '',
            indent ? 'pl-5' : ''
          )
        }
        color="foreground"
        to={typeof navLink.href === 'string' ? navLink.href : navLink.href(user)}
        target={navLink.newTab ? '_blank' : '_self'}
        onClick={onLinkClick}
      >
        {t(navLink.label)}
      </NavLink>
    </NavbarMenuItem>
  )
}

export function MobileNavMenu(props: MobileNavMenuProps): ReactNode {
  const { onLinkClick } = props

  const { user } = use(AuthorizationContext)
  const { t } = useTranslation()

  const [expandedNavItem, setExpandedNavItem] = useState<string | undefined>(undefined)

  function handleMenuItemToggle(id: string) {
    setExpandedNavItem(prev => {
      if (prev === id) {
        return undefined
      } else {
        return id
      }
    })
  }

  return (
    <NavbarMenu>
      <div className="mx-4 mt-2 flex flex-col gap-2">
        {SITE_CONFIG.navItems
          .filter((item) => item.predicate(user))
          .map((item) => {
            if (Object.hasOwn(item, 'children')) {
              const node = item as NavItemNode
              const visibleChildren = node.children.filter((subItem) => subItem.predicate(user))
              const Icon = expandedNavItem === node.id ? FaChevronUp : FaChevronDown

              return (
                <Fragment key={node.id}>
                  <NavbarMenuItem>
                    <button type="button" className="text-medium flex gap-1.5 items-center" onClick={() => handleMenuItemToggle(node.id)}>
                      {t(node.label)}
                      <Icon size={15} />
                    </button>
                  </NavbarMenuItem>
                  {expandedNavItem === node.id
                    ? visibleChildren.map((subItem) => renderLink(subItem, user, t, onLinkClick, true))
                    : null}
                </Fragment>
              )
            } else {
              return renderLink(item as NavItemLeaf, user, t, onLinkClick)
            }
          })}
      </div>

      <div className="flex sm:hidden flex-col grow justify-end">
        <Divider />
        <div className="mx-4 my-2 flex gap-2">
          <AccountMenu dropdownPlacement="top" onNavigate={onLinkClick} />
          {user == null && <LanguageSwitcher className="ml-auto" />}
        </div>
      </div>
    </NavbarMenu>
  )
}

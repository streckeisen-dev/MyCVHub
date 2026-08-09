import {
  link as linkStyles,
  Navbar as HeroUINavbar,
  NavbarBrand,
  NavbarContent,
  NavbarItem,
  NavbarMenuToggle
} from '@/components/ui/Navigation.tsx'
import clsx from 'clsx'

import { FaGithub } from 'react-icons/fa'

import Logo from './Logo.tsx'
import { ThemeSwitch } from '@/components/nav/ThemeSwitch.tsx'

import { AccountMenu } from '@/components/nav/AccountMenu.tsx'
import { LanguageSwitcher } from '@/components/nav/LanguageSwitcher.tsx'
import { useTranslation } from 'react-i18next'
import { Link, NavLink, useNavigate } from 'react-router-dom'

import classes from './Navbar.module.css'
import { ReactNode, use, useState } from 'react'
import { AuthorizationContext, AuthorizedUser } from '@/context/AuthorizationContext.tsx'
import { getRoutePath, NavItemConfig, NavItemLeaf, NavItemNode, RouteId, SITE_CONFIG } from '@/config/RouteTree.tsx'
import { ExternalLink } from '@/components/ExternalLink.tsx'
import { TFunction } from 'i18next'
import { FaChevronDown } from 'react-icons/fa6'
import { MobileNavMenu } from '@/components/nav/MobileNavMenu.tsx'
import { Dropdown, Label } from '@heroui/react'
import { Key } from '@react-types/shared'
import { withRouterBasename } from '@/config/RouterConfig.ts'

function renderNavLinks(
  navLinks: NavItemConfig[],
  user: AuthorizedUser | undefined,
  t: TFunction,
  navigate: (to: string) => void
): ReactNode {
  return navLinks
    .filter((item) => item.predicate(user))
    .map((item) => {
      if (Object.hasOwn(item, 'children')) {
        const node: NavItemNode = item as NavItemNode
        const visibleChildren = node.children.filter((subItem) => subItem.predicate(user))

        function handleDropdownAction(key: Key) {
          const selectedItem = visibleChildren.find((subItem) => subItem.id === key)
          if (selectedItem == null) return

          const href =
            typeof selectedItem.href === 'string' ? selectedItem.href : selectedItem.href(user)

          if (selectedItem.newTab) {
            window.open(withRouterBasename(href), '_blank', 'noopener,noreferrer')
          } else {
            navigate(href)
          }
        }

        return (
          <Dropdown key={node.id}>
            <NavbarItem>
              <Dropdown.Trigger type="button" className="flex gap-2 bg-transparent p-0 text-medium">
                {t(node.label)}
                <FaChevronDown size={15} className="self-center-safe" />
              </Dropdown.Trigger>
            </NavbarItem>
            <Dropdown.Popover>
              <Dropdown.Menu aria-label={t(node.label)} onAction={handleDropdownAction}>
                {visibleChildren.map((subItem) => (
                  <Dropdown.Item key={subItem.id} id={subItem.id} textValue={t(subItem.label)}>
                    <Label>{t(subItem.label)}</Label>
                  </Dropdown.Item>
                ))}
              </Dropdown.Menu>
            </Dropdown.Popover>
          </Dropdown>
        )
      } else {
        const leaf: NavItemLeaf = item as NavItemLeaf
        return <NavbarItem key={item.id}>{renderLink(leaf, user, t)}</NavbarItem>
      }
    })
}

function renderLink(
  navLink: NavItemLeaf,
  user: AuthorizedUser | undefined,
  t: TFunction
): ReactNode {
  return (
    <NavLink
      className={({ isActive }) =>
        clsx(
          linkStyles({ color: 'foreground' }),
          'data-[active=true]:text-primary data-[active=true]:font-medium',
          isActive ? classes.activeLink : ''
        )
      }
      color="foreground"
      to={typeof navLink.href === 'string' ? navLink.href : navLink.href(user)}
      target={navLink.newTab ? '_blank' : '_self'}
    >
      {t(navLink.label)}
    </NavLink>
  )
}

export const Navbar = () => {
  const { t } = useTranslation()
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false)
  const navigate = useNavigate()

  const { user } = use(AuthorizationContext)

  function handleLinkClick() {
    setIsMenuOpen(false)
  }

  return (
    <HeroUINavbar
      maxWidth="full"
      position="sticky"
      isMenuOpen={isMenuOpen}
      onMenuOpenChange={setIsMenuOpen}
    >
      <NavbarContent className="basis-1/5 sm:basis-full" justify="start">
        <NavbarBrand className="gap-3 max-w-fit">
          <Link
            className="flex justify-start items-center gap-1"
            to={getRoutePath(RouteId.Home)}
            onClick={handleLinkClick}
          >
            <Logo />
            <p className="font-bold text-inherit">MyCVHub</p>
          </Link>
        </NavbarBrand>
        <div className="hidden xl:flex gap-4 justify-start ml-20">
          {renderNavLinks(SITE_CONFIG.navItems, user, t, navigate)}
        </div>
      </NavbarContent>

      <NavbarContent className="basis-1/5 sm:basis-full" justify="end">
        <NavbarItem className="hidden sm:flex">
          <AccountMenu />
        </NavbarItem>
        <NavbarItem className="flex gap-2">
          <ExternalLink href={SITE_CONFIG.links.github} title="GitHub">
            <FaGithub className="text-default-500" size={25} />
          </ExternalLink>
          <ThemeSwitch />
        </NavbarItem>
        {user == null && (
          <NavbarItem className="hidden sm:flex">
            <LanguageSwitcher />
          </NavbarItem>
        )}
        <NavbarMenuToggle className="xl:hidden" />
      </NavbarContent>

      <MobileNavMenu onLinkClick={handleLinkClick} />
    </HeroUINavbar>
  )
}

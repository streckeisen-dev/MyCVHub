import {
  Divider,
  Link,
  link as linkStyles,
  Navbar as HeroUINavbar,
  NavbarBrand,
  NavbarContent,
  NavbarItem,
  NavbarMenu,
  NavbarMenuItem,
  NavbarMenuToggle
} from '@heroui/react'
import clsx from 'clsx'

import { FaGithub } from 'react-icons/fa'

import Logo from './Logo.tsx'
import { ThemeSwitch } from '@/components/nav/ThemeSwitch.tsx'

import { AccountMenu } from '@/components/nav/AccountMenu.tsx'
import { LanguageSwitcher } from '@/components/nav/LanguageSwitcher.tsx'
import { useTranslation } from 'react-i18next'
import { NavLink } from 'react-router-dom'

import classes from './Navbar.module.css'
import { use, useState } from 'react'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { SITE_CONFIG } from '@/config/RouteTree.tsx'
import { ExternalLink } from '@/components/ExternalLink.tsx'

export const Navbar = () => {
  const { t } = useTranslation()
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false)

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
          <Link className="flex justify-start items-center gap-1" color="foreground" href="/">
            <Logo />
            <p className="font-bold text-inherit">MyCVHub</p>
          </Link>
        </NavbarBrand>
        <div className="hidden xl:flex gap-4 justify-start ml-20">
          {SITE_CONFIG.navItems
            .filter((item) => item.predicate(user))
            .map((item) => (
              <NavbarItem key={item.id}>
                <NavLink
                  className={({ isActive }) =>
                    clsx(
                      linkStyles({ color: 'foreground' }),
                      'data-[active=true]:text-primary data-[active=true]:font-medium',
                      isActive ? classes.activeLink : ''
                    )
                  }
                  color="foreground"
                  to={typeof item.href === 'string' ? item.href : item.href(user)}
                  target={item.newTab ? '_blank' : '_self'}
                >
                  {t(item.label)}
                </NavLink>
              </NavbarItem>
            ))}
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

      <NavbarMenu>
        <div className="mx-4 mt-2 flex flex-col gap-2">
          {SITE_CONFIG.navItems
            .filter((item) => item.predicate(user))
            .map((item) => (
              <NavbarMenuItem key={item.id}>
                <NavLink
                  className={({ isActive }) =>
                    clsx(
                      linkStyles({ color: 'foreground' }),
                      'data-[active=true]:text-primary data-[active=true]:font-medium',
                      isActive ? classes.activeLink : ''
                    )
                  }
                  color="foreground"
                  to={typeof item.href === 'string' ? item.href : item.href(user)}
                  target={item.newTab ? '_blank' : '_self'}
                  onClick={handleLinkClick}
                >
                  {t(item.label)}
                </NavLink>
              </NavbarMenuItem>
            ))}
        </div>

        <div className="flex sm:hidden flex-col grow justify-end">
          <Divider />
          <div className="mx-4 my-2 flex gap-2">
            <AccountMenu dropdownPlacement="top" onNavigate={handleLinkClick} />
            {user == null && <LanguageSwitcher className="ml-auto" />}
          </div>
        </div>
      </NavbarMenu>
    </HeroUINavbar>
  )
}

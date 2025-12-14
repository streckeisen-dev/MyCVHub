import {
  Button,
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

import { FaGithub, FaHeart } from 'react-icons/fa'

import Logo from './Logo.tsx'
import { ThemeSwitch } from '@/components/nav/ThemeSwitch.tsx'

import { AccountMenu } from '@/components/nav/AccountMenu.tsx'
import { LanguageSwitcher } from '@/components/nav/LanguageSwitcher.tsx'
import { useTranslation } from 'react-i18next'
import { NavLink } from 'react-router-dom'

import classes from './Navbar.module.css'
import { use } from 'react'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { SITE_CONFIG } from '@/config/RouteTree.tsx'

export const Navbar = () => {
  const { t } = useTranslation()

  const { user } = use(AuthorizationContext)

  return (
    <HeroUINavbar maxWidth="full" position="sticky">
      <NavbarContent className="basis-1/5 sm:basis-full" justify="start">
        <NavbarBrand className="gap-3 max-w-fit">
          <Link
            className="flex justify-start items-center gap-1"
            color="foreground"
            href="/"
          >
            <Logo />
            <p className="font-bold text-inherit">MyCVHub</p>
          </Link>
        </NavbarBrand>
        <div className="hidden lg:flex gap-4 justify-start ml-2">
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

      <NavbarContent
        className="hidden sm:flex basis-1/5 sm:basis-full"
        justify="end"
      >
        <NavbarItem className="hidden md:flex">
          <AccountMenu />
        </NavbarItem>
        <NavbarItem className="hidden sm:flex gap-2">
          <Link isExternal href={SITE_CONFIG.links.github} title="GitHub">
            <FaGithub className="text-default-500" size={25} />
          </Link>
          <ThemeSwitch />
        </NavbarItem>
        <NavbarItem className="hidden md:flex">
          <Button
            isExternal
            as={Link}
            className="text-sm font-normal text-default-600 bg-default-100"
            href={SITE_CONFIG.links.sponsor}
            startContent={<FaHeart className="text-danger" />}
            variant="flat"
          >
            Sponsor
          </Button>
        </NavbarItem>
        {user == null && (
          <NavbarItem>
            <LanguageSwitcher />
          </NavbarItem>
        )}
      </NavbarContent>

      <NavbarContent className="sm:hidden basis-1 pl-4" justify="end">
        <Link isExternal href={SITE_CONFIG.links.github}>
          <FaGithub className="text-default-500" />
        </Link>
        <ThemeSwitch />
        <NavbarMenuToggle />
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
                >
                  {item.label}
                </NavLink>
              </NavbarMenuItem>
            ))}
        </div>

        <div className="flex flex-col flex-grow justify-end">
          <Divider />
          <div className="mx-4 my-2 flex gap-2">
            <AccountMenu dropdownPlacement="top" />
          </div>
        </div>
      </NavbarMenu>
    </HeroUINavbar>
  )
}

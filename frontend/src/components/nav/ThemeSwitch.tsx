import { useTheme } from '@heroui/use-theme'
import { FaMoon, FaSun } from 'react-icons/fa'
import { ReactNode } from 'react'

export function ThemeSwitch(): ReactNode {
  const { theme, setTheme } = useTheme()

  function toggleTheme() {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const isDark = theme === 'dark'

  const Icon =  isDark ? FaSun : FaMoon
  return <Icon onClick={toggleTheme} size={25} className="cursor-pointer text-default-500 hover:text-default-400" />
}
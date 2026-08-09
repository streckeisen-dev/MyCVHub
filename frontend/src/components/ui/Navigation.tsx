import { CSSProperties, ReactNode, createContext, useContext, useMemo, useState } from 'react'
import { FaBars } from 'react-icons/fa6'

const MENU_ID = 'main-navigation-menu'

const MenuContext = createContext<{ isOpen: boolean; setOpen: (open: boolean) => void }>({
  isOpen: false,
  setOpen: () => undefined
})

export const link = (_options?: unknown) =>
  'whitespace-nowrap text-foreground transition-colors hover:text-primary'

export type NavbarProps = Readonly<{
  children?: ReactNode
  className?: string
  isMenuOpen?: boolean
  maxWidth?: string
  onMenuOpenChange?: (open: boolean) => void
  position?: string
  style?: CSSProperties
}>

export function Navbar(props: NavbarProps) {
  const { children, className, isMenuOpen, maxWidth: _maxWidth, onMenuOpenChange, position: _position, style } = props
  const [fallbackOpen, setFallbackOpen] = useState(false)
  const isOpen = isMenuOpen ?? fallbackOpen
  const setOpen = onMenuOpenChange ?? setFallbackOpen
  const contextValue = useMemo(() => ({ isOpen, setOpen }), [isOpen, setOpen])

  return (
    <MenuContext.Provider value={contextValue}>
      <nav
        className={[
          'sticky top-0 z-40 flex min-h-[4.5rem] w-full items-center gap-4 border-b border-default-100 bg-background py-3 pl-6 pr-2 sm:px-6 relative',
          className
        ]
          .filter(Boolean)
          .join(' ')}
        style={style}
      >
        {children}
      </nav>
    </MenuContext.Provider>
  )
}

export const NavbarBrand = ({
  children,
  className
}: {
  children?: ReactNode
  className?: string
}) => <div className={['flex items-center', className].filter(Boolean).join(' ')}>{children}</div>
export const NavbarContent = ({
  children,
  className,
  justify
}: {
  children?: ReactNode
  className?: string
  justify?: string
}) => (
  <div
    className={[
      'flex min-w-0 items-center gap-4',
      justify === 'end' && 'ml-auto justify-end',
      justify === 'center' && 'justify-center',
      justify === 'start' && 'justify-start',
      className
    ]
      .filter(Boolean)
      .join(' ')}
  >
    {children}
  </div>
)
export const NavbarItem = ({
  children,
  className
}: {
  children?: ReactNode
  className?: string
}) => <div className={className}>{children}</div>
export const NavbarMenuItem = NavbarItem
export const NavbarMenu = ({
  children,
  className
}: {
  children?: ReactNode
  className?: string
}) => {
  const { isOpen } = useContext(MenuContext)
  if (!isOpen) return null
  return (
    <div
      id={MENU_ID}
      className={[
        'absolute left-0 right-0 top-full z-50 flex max-h-[calc(100dvh-4.5rem)] min-h-[calc(100dvh-4.5rem)] flex-col overflow-y-auto border-t border-default-100 bg-background px-2 py-4 shadow-lg xl:hidden',
        className
      ]
        .filter(Boolean)
        .join(' ')}
    >
      {children}
    </div>
  )
}
export const NavbarMenuToggle = ({ className }: { className?: string }) => {
  const { isOpen, setOpen } = useContext(MenuContext)
  return (
    <button
      aria-controls={MENU_ID}
      aria-expanded={isOpen}
      aria-label={isOpen ? 'Close menu' : 'Open menu'}
      className={[
        'inline-flex h-10 w-10 items-center justify-center text-default-500 transition-colors hover:text-foreground',
        className
      ]
        .filter(Boolean)
        .join(' ')}
      type="button"
      onClick={() => setOpen(!isOpen)}
    >
      <FaBars size={22} />
    </button>
  )
}

import clsx from 'clsx'
import { PropsWithChildren, ReactNode } from 'react'
import { h2, h3, title } from '@/styles/primitives.ts'

type PageSize = 'narrow' | 'default' | 'wide' | 'full'

export type PageProps = Readonly<
  PropsWithChildren & {
    className?: string
    size?: PageSize
  }
>

export type PageHeaderProps = Readonly<
  PropsWithChildren & {
    align?: 'start' | 'center'
    className?: string
  }
>

const pageSizeClasses: Record<PageSize, string> = {
  narrow: 'max-w-3xl',
  default: 'max-w-5xl',
  wide: 'max-w-7xl',
  full: 'max-w-none'
}

export function Page(props: PageProps): ReactNode {
  const { children, className, size = 'default' } = props

  return (
    <section
      className={clsx(
        'mx-auto flex w-full flex-col gap-6 py-6 md:gap-7 md:py-8',
        pageSizeClasses[size],
        className
      )}
    >
      {children}
    </section>
  )
}

export function PageHeader(props: PageHeaderProps): ReactNode {
  const { align = 'start', children, className } = props

  return (
    <div
      className={clsx(
        'flex w-full flex-col gap-3',
        align === 'center' && 'items-center text-center',
        className
      )}
    >
      {children}
    </div>
  )
}

export function PageIntro(props: PropsWithChildren<{ className?: string }>): ReactNode {
  const { children, className } = props

  return (
    <p className={clsx('max-w-3xl text-sm leading-6 text-default-600 md:text-base', className)}>
      {children}
    </p>
  )
}

export function PageTitle(props: PropsWithChildren<{ className?: string }>): ReactNode {
  const { children, className } = props

  return <h1 className={title({ size: 'sm', class: className })}>{children}</h1>
}

export function DetailTitle(props: PropsWithChildren<{ className?: string }>): ReactNode {
  const { children, className } = props

  return <h1 className={h2({ class: className })}>{children}</h1>
}

export function SectionTitle(props: PropsWithChildren<{ className?: string }>): ReactNode {
  const { children, className } = props

  return <h2 className={h3({ class: className })}>{children}</h2>
}

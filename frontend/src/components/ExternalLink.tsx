import { AnchorHTMLAttributes } from 'react'
import { LinkProps } from '@heroui/react'

export type ExternalLinkProps = AnchorHTMLAttributes<HTMLAnchorElement> & {
  color?: LinkProps['color']
}

export function ExternalLink(props: ExternalLinkProps) {
  const { color, children, className, ...linkProps } = props

  const textColor = `text-${color ?? 'primary'}`
  return (
    <a
      {...linkProps}
      target="_blank"
      rel="noreferrer"
      className={`${textColor} hover:opacity-hover active:opacity-disabled ${className ?? ''}`}
    >
      {children}
    </a>
  )
}

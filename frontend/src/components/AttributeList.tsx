import { Fragment, ReactNode } from 'react'
import clsx from 'clsx'

export interface Attribute {
  name: string
  value: string | ReactNode
  fullWidth?: boolean
}

export type AttributeListProps = Readonly<{
  attributes: Attribute[]
  className?: string
}>

export function AttributeList(props: AttributeListProps) {
  const { attributes, className } = props
  return (
    <dl
      className={clsx(
        'grid w-full grid-cols-1 items-start gap-x-6 gap-y-3 sm:grid-cols-[minmax(9rem,14rem)_minmax(0,1fr)]',
        className
      )}
    >
      {attributes.map((attribute) => (
        <Fragment key={attribute.name}>
          <dt
            className={clsx(
              'text-sm font-semibold text-default-500',
              attribute.fullWidth && 'sm:col-span-2'
            )}
          >
            {attribute.name}
          </dt>
          <dd
            className={clsx(
              'min-w-0 text-sm leading-6 text-foreground',
              attribute.fullWidth && 'sm:col-span-2'
            )}
          >
            {attribute.value}
          </dd>
        </Fragment>
      ))}
    </dl>
  )
}

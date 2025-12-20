import { Fragment, ReactNode } from 'react'

export interface Attribute {
  name: string
  value: string | ReactNode
}

export type AttributeListProps = Readonly<{
  attributes: Attribute[]
  className?: string
}>

export function AttributeList(props: AttributeListProps) {
  const { attributes, className } = props
  return (
    <dl className={`grid grid-cols-2 xl:grid-cols-[25%_auto] gap-y-2 w-full ${className}`}>
      {attributes.map((attribute) => (
        <Fragment key={attribute.name}>
          <dt className="font-bold">{attribute.name}</dt>
          <dd>{attribute.value}</dd>
        </Fragment>
      ))}
    </dl>
  )
}

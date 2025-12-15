import { Fragment } from 'react'

export interface Attribute {
  name: string;
  value: string;
}

export type AttributeListProps = Readonly<{
  attributes: Attribute[];
}>

export function AttributeList(props: AttributeListProps) {
  const { attributes } = props
  return (
    <dl className="grid grid-cols-2 gap-y-2 gap-x-4 sm:gap-x-8 lg:gap-x-30 w-full">
      {attributes.map((attribute) => (
        <Fragment key={attribute.name}>
          <dt className="font-bold">{attribute.name}</dt>
          <dd>{attribute.value}</dd>
        </Fragment>
      ))}
    </dl>
  )
}

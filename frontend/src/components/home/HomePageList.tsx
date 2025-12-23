import { ReactNode } from 'react'

export interface HomePageListEntry {
  title: string
  description: string
}

export type HomePageListProps = Readonly<{
  entries: HomePageListEntry[]
}>

export function HomePageList(props: HomePageListProps): ReactNode {
  const { entries } = props
  return (
    <div className="flex flex-col gap-2">
      {entries.map((entry) => (
        <div key={entry.title} className="">
          <p className="font-bold text-large">{entry.title}</p>
          <p className="pl-5">{entry.description}</p>
        </div>
      ))}
    </div>
  )
}

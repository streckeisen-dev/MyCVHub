import { Checkbox, Switch } from '@/components/ui/Fields.tsx'
import { useState } from 'react'
import { FaCaretDown, FaCaretRight } from 'react-icons/fa6'

import { useTranslation } from 'react-i18next'

export interface SelectedCvContent {
  id: number
  includeDescription: boolean
}

export interface CvContentTreeLeaf {
  id: number
  title: string
  selected: boolean
  includeDescription: boolean
}

export type CvContentTreeRootProps = Readonly<{
  title: string
  content: CvContentTreeLeaf[]
  onChange: (selected: SelectedCvContent[]) => void
  disabled: boolean
  errorMessage?: string
}>

function toSelectedCvContent(leaf: CvContentTreeLeaf): SelectedCvContent {
  return {
    id: leaf.id,
    includeDescription: leaf.includeDescription
  }
}

export function CvContentTreeRoot(props: CvContentTreeRootProps) {
  const { t } = useTranslation()
  const { title, content, onChange, disabled, errorMessage } = props

  const [isExpanded, setIsExpanded] = useState(false)
  const isRootSelected = content.every((leaf) => leaf.selected)
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleRootChange(isSelected: boolean) {
    if (isSelected) {
      onChange(content.map(toSelectedCvContent))
    } else {
      onChange([])
    }
  }

  function handleLeafChange(id: number, isSelected: boolean) {
    if (disabled) return
    if (isSelected) {
      onChange([
        ...content.map(toSelectedCvContent),
        {
          id,
          includeDescription: true
        }
      ])
    } else {
      onChange(content.filter((l) => l.id !== id && l.selected).map(toSelectedCvContent))
    }
  }

  function handleDescriptionChange(id: number, includeDescription: boolean) {
    if (disabled) return
    onChange([
      ...content.filter((l) => l.id !== id && l.selected).map(toSelectedCvContent),
      {
        id: id,
        includeDescription: includeDescription
      }
    ])
  }

  return (
    <div>
      <div className="flex items-center gap-3 rounded-md px-2 py-2 text-sm hover:bg-default/5">
        <Icon onClick={toggleExpand} className="shrink-0 cursor-pointer text-default-500" />
        <Checkbox
          isDisabled={disabled}
          isSelected={isRootSelected}
          onValueChange={handleRootChange}
          isIndeterminate={!isRootSelected && content.some((leaf) => leaf.selected)}
        />
        <p className="min-w-0 font-medium text-foreground">{title}</p>
      </div>
      {errorMessage && <p className="px-2 text-sm text-danger">{errorMessage}</p>}
      {isExpanded && (
        <div className="flex flex-col gap-1">
          {content.map((leaf) => (
            <div
              key={leaf.id}
              className="grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 rounded-md px-2 py-2 pl-10 text-sm hover:bg-default/5"
            >
              <Checkbox
                isDisabled={disabled}
                isSelected={leaf.selected}
                onValueChange={(isSelected) => handleLeafChange(leaf.id, isSelected)}
              />
              <p className="min-w-0 text-wrap leading-5 text-foreground">{leaf.title}</p>
              <Switch
                size="sm"
                isDisabled={!leaf.selected || disabled}
                className="shrink-0"
                isSelected={leaf.includeDescription}
                onValueChange={(val: boolean) => handleDescriptionChange(leaf.id, val)}
              >
                <p className="text-sm">{t('cv.showDescription')}</p>
              </Switch>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

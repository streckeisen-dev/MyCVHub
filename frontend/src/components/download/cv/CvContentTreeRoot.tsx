import { useState } from 'react'
import { FaCaretDown, FaCaretRight } from 'react-icons/fa6'
import { Checkbox, Switch } from '@heroui/react'
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
      <div className="flex gap-x-2 p-2">
        <Icon onClick={toggleExpand} className="cursor-pointer self-center" />
        <Checkbox
          isDisabled={disabled}
          isSelected={isRootSelected}
          onValueChange={handleRootChange}
          isIndeterminate={!isRootSelected && content.some((leaf) => leaf.selected)}
        />
        <p>{title}</p>
      </div>
      {errorMessage && <p className="text-danger text-sm">{errorMessage}</p> }
      {isExpanded && (
        <div>
          {content.map((leaf) => (
            <div key={leaf.id} className="flex gap-x-2 p-2 pl-12 items-center">
              <Checkbox
                isDisabled={disabled}
                isSelected={leaf.selected}
                onValueChange={(isSelected) => handleLeafChange(leaf.id, isSelected)}
              />
              <p className="grow text-wrap max-w-45">{leaf.title}</p>
              <Switch
                size="sm"
                isDisabled={!leaf.selected || disabled}
                className="max-w-30"
                isSelected={leaf.includeDescription}
                onValueChange={(val) => handleDescriptionChange(leaf.id, val)}
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

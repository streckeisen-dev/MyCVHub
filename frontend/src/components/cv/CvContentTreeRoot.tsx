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
}>

function toSelectedCvContent(leaf: CvContentTreeLeaf): SelectedCvContent {
  return {
    id: leaf.id,
    includeDescription: leaf.includeDescription
  }
}

export function CvContentTreeRoot(props: CvContentTreeRootProps) {
  const { t } = useTranslation()
  const { title, content, onChange } = props

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
      <div className="grid grid-cols-[5%_5%_auto_10%] gap-3 p-2">
        <Icon onClick={toggleExpand} className="cursor-pointer self-center" />
        <Checkbox
          isSelected={isRootSelected}
          onValueChange={handleRootChange}
          isIndeterminate={!isRootSelected && content.some((leaf) => leaf.selected)}
        />
        <p>{title}</p>
      </div>
      {isExpanded && (
        <div>
          {content.map((leaf) => (
            <div key={leaf.id} className="grid grid-cols-[12%_5%_auto_30%] gap-3 p-2 items-center">
              <span></span>
              <Checkbox
                isSelected={leaf.selected}
                onValueChange={(isSelected) => handleLeafChange(leaf.id, isSelected)}
              />
              <p>{leaf.title}</p>
              <Switch
                className="col-span-2 col-start-3 md:col-start-auto md:col-span-1"
                isSelected={leaf.includeDescription}
                onValueChange={(val) => handleDescriptionChange(leaf.id, val)}
              >
                {t('cv.showDescription')}
              </Switch>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

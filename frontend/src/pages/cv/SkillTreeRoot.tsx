import { ReactNode, useState } from 'react'
import { SelectedCvContent } from '@/pages/cv/CvContentTreeRoot.tsx'
import { Checkbox } from '@heroui/react'
import { FaCaretDown, FaCaretRight } from 'react-icons/fa6'
import { useTranslation } from 'react-i18next'

export interface SkillCategories {
  title: string;
  children: SkillLeaf[];
}

export interface SkillLeaf {
  id: number;
  title: string;
  selected: boolean;
}

export type SkillTreeRootProps = Readonly<{
  content: SkillCategories[];
  onChange: (selected: SelectedCvContent[]) => void;
}>

export function SkillTreeRoot(props: SkillTreeRootProps): ReactNode {
  const { t } = useTranslation()
  const { content, onChange } = props

  const [isExpanded, setIsExpanded] = useState(false)
  const isRootSelected = content.every((c) =>
    c.children.every((leaf) => leaf.selected)
  )
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleRootChange(isSelected: boolean) {
    if (isSelected) {
      onChange(
        content.flatMap((c) =>
          c.children.map((l) => {
            return {
              id: l.id,
              includeDescription: false
            }
          })
        )
      )
    } else {
      onChange([])
    }
  }

  function handleSelected(selected: number[]) {
    const alreadySelected: SelectedCvContent[] = content.flatMap((c) =>
      c.children.filter(l => l.selected).map((l) => {
        return {
          id: l.id,
          includeDescription: false
        }
      })
    )
    onChange([
      ...alreadySelected,
      ...selected.map((id) => {
        return {
          id,
          includeDescription: false
        }
      })
    ])
  }

  function handleDeselected(deselected: number[]) {
    const selected: SelectedCvContent[] = content
      .flatMap((c) =>
        c.children.filter(l => l.selected).map((l) => l.id)
      )
      .filter(id => !deselected.includes(id))
      .map(id => {
        return {
          id,
          includeDescription: false
        }
      })
    onChange(selected)
  }

  return (
    <div>
      <div className="grid grid-cols-[5%_5%_auto_10%] gap-3 p-2">
        <Icon onClick={toggleExpand} className="cursor-pointer self-center" />
        <Checkbox
          isSelected={isRootSelected}
          onValueChange={handleRootChange}
          isIndeterminate={
            !isRootSelected &&
            content.some((c) => c.children.some((leaf) => leaf.selected))
          }
        />
        <p>{t('skills.title')}</p>
      </div>
      {isExpanded && (
        <div>
          {content.map((category) => (
            <SkillCategoryLeaf
              key={category.title}
              title={category.title}
              content={category.children}
              onSelect={handleSelected}
              onDeselect={handleDeselected}
            />
          ))}
        </div>
      )}
    </div>
  )
}

type SkillCategoryLeafProps = Readonly<{
  title: string;
  content: SkillLeaf[];
  onSelect: (selected: number[]) => void;
  onDeselect: (deselected: number[]) => void;
}>

function SkillCategoryLeaf(props: SkillCategoryLeafProps): ReactNode {
  const {title, content, onSelect, onDeselect} = props
  
  const [isExpanded, setIsExpanded] = useState(false)
  const isCategorySelected = content.every((leaf) => leaf.selected)
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleCategoryChange(isSelected: boolean) {
    const ids = content.map((l) => l.id)
    if (isSelected) {
      onSelect(ids)
    } else {
      onDeselect(ids)
    }
  }

  function handleLeafChange(id: number, isSelected: boolean) {
    const ids = [id]
    if (isSelected) {
      onSelect(ids)
    } else {
      onDeselect(ids)
    }
  }

  return (
    <>
      <div className="grid grid-cols-[5%_5%_auto_10%] gap-3 p-2 pl-12">
        <Icon onClick={toggleExpand} className="cursor-pointer self-center" />
        <Checkbox
          isSelected={isCategorySelected}
          onValueChange={handleCategoryChange}
          isIndeterminate={
            !isCategorySelected && content.some((leaf) => leaf.selected)
          }
        />
        <p>{title}</p>
      </div>
      {isExpanded && (
        <div>
          {content.map((leaf) => (
            <div
              key={leaf.id}
              className="grid grid-cols-[5%_auto] gap-3 p-2 pl-30 items-center"
            >
              <Checkbox
                isSelected={leaf.selected}
                onValueChange={(isSelected) =>
                  handleLeafChange(leaf.id, isSelected)
                }
              />
              <p>{leaf.title}</p>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

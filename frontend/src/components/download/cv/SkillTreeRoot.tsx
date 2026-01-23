import { ReactNode, useState } from 'react'
import { Checkbox } from '@heroui/react'
import { FaCaretDown, FaCaretRight } from 'react-icons/fa6'
import { useTranslation } from 'react-i18next'

export interface SkillCategories {
  title: string
  children: SkillLeaf[]
}

export interface SkillLeaf {
  id: number
  title: string
  selected: boolean
}

export type SkillTreeRootProps = Readonly<{
  content: SkillCategories[]
  onChange: (selected: number[]) => void
  disabled: boolean
  errorMessage?: string
}>

export function SkillTreeRoot(props: SkillTreeRootProps): ReactNode {
  const { t } = useTranslation()
  const { content, onChange, disabled, errorMessage } = props

  const [isExpanded, setIsExpanded] = useState(false)
  const isRootSelected = content.every((c) => c.children.every((leaf) => leaf.selected))
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleRootChange(isSelected: boolean) {
    if (disabled) return
    if (isSelected) {
      onChange(content.flatMap((c) => c.children.map((l) => l.id)))
    } else {
      onChange([])
    }
  }

  function handleSelected(selected: number[]) {
    if (disabled) return
    const alreadySelected: number[] = content.flatMap((c) =>
      c.children.filter((l) => l.selected).map((l) => l.id)
    )
    onChange([...alreadySelected, ...selected])
  }

  function handleDeselected(deselected: number[]) {
    if (disabled) return
    const selected: number[] = content
      .flatMap((c) => c.children.filter((l) => l.selected).map((l) => l.id))
      .filter((id) => !deselected.includes(id))
    onChange(selected)
  }

  return (
    <div>
      <div className="flex gap-2 p-2">
        <Icon onClick={toggleExpand} className="cursor-pointer self-center" />
        <Checkbox
          isDisabled={disabled}
          isSelected={isRootSelected}
          onValueChange={handleRootChange}
          isIndeterminate={
            !isRootSelected && content.some((c) => c.children.some((leaf) => leaf.selected))
          }
        />
        <p>{t('skills.title')}</p>
      </div>
      {errorMessage && <p className="text-danger text-sm">{errorMessage}</p>}
      {isExpanded && (
        <div>
          {content.map((category) => (
            <SkillCategoryLeaf
              key={category.title}
              title={category.title}
              content={category.children}
              onSelect={handleSelected}
              onDeselect={handleDeselected}
              disabled={disabled}
            />
          ))}
        </div>
      )}
    </div>
  )
}

type SkillCategoryLeafProps = Readonly<{
  title: string
  content: SkillLeaf[]
  onSelect: (selected: number[]) => void
  onDeselect: (deselected: number[]) => void
  disabled: boolean
}>

function SkillCategoryLeaf(props: SkillCategoryLeafProps): ReactNode {
  const { title, content, onSelect, onDeselect, disabled } = props

  const [isExpanded, setIsExpanded] = useState(false)
  const isCategorySelected = content.every((leaf) => leaf.selected)
  const toggleExpand = () => setIsExpanded((prev) => !prev)
  const Icon = isExpanded ? FaCaretDown : FaCaretRight

  function handleCategoryChange(isSelected: boolean) {
    if (disabled) return
    const ids = content.map((l) => l.id)
    if (isSelected) {
      onSelect(ids)
    } else {
      onDeselect(ids)
    }
  }

  function handleLeafChange(id: number, isSelected: boolean) {
    if (disabled) return
    const ids = [id]
    if (isSelected) {
      onSelect(ids)
    } else {
      onDeselect(ids)
    }
  }

  return (
    <>
      <div className="flex gap-2 p-2 pl-8">
        <Icon onClick={toggleExpand} className="cursor-pointer self-center" />
        <Checkbox
          isDisabled={disabled}
          isSelected={isCategorySelected}
          onValueChange={handleCategoryChange}
          isIndeterminate={!isCategorySelected && content.some((leaf) => leaf.selected)}
        />
        <p>{title}</p>
      </div>
      {isExpanded && (
        <div>
          {content.map((leaf) => (
            <div key={leaf.id} className="flex gap-2 p-2 pl-20 items-center">
              <Checkbox
                isDisabled={disabled}
                isSelected={leaf.selected}
                onValueChange={(isSelected) => handleLeafChange(leaf.id, isSelected)}
              />
              <p>{leaf.title}</p>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import moment from 'moment'
import { Button } from '@heroui/react'
import { FaPen, FaTrash } from 'react-icons/fa6'

export interface CvEntry {
  id: number | undefined;
  title: string;
  topRight: string | ReactNode;
  bottomLeft: string;
  start: string;
  end: string | undefined;
  description: string | undefined;
}

export type ListModificationEvent = (id: number) => void;

export interface CvListEntryProps {
  value: CvEntry;
  hasActions: boolean | undefined;
  onEdit?: ListModificationEvent;
  onDelete?: ListModificationEvent;
}

export function CvListEntry(props: CvListEntryProps): ReactNode {
  const { t } = useTranslation()
  const {value, hasActions, onEdit, onDelete} = props
  
  const startDate = moment(value.start).format('MMM YYYY')
  const endDate = value.end
    ? moment(value.end).format('MMM YYYY')
    : t('date.today')
  const duration = `${startDate} - ${endDate}`

  const entryClasses =
    'grid grid-cols-12 gap-2 gap-y-2 md:gap-y-0 bg-default-100 p-5 rounded-lg' +
    (hasActions ? ' md: grid-cols-10' : '')
  const fixedClasses = 'col-span-8'
  const adaptableClasses = hasActions
    ? 'col-span-4 lg:col-span-2'
    : 'col-span-4'
  const actionClasses =
    'rounded-full w-min col-span-3 sm:col-span-2 lg:col-span-1 lg:justify-self-end min-w-1'

  function handleEdit() {
    if (value.id && onEdit) {
      onEdit(value.id)
    }
  }

  function handleDelete() {
    if (value.id && onDelete) {
      onDelete(value.id)
    }
  }

  return (
    <div className={entryClasses}>
      <p className={`font-bold text-lg ${fixedClasses}`}>{value.title}</p>
      <p className={adaptableClasses}>{value.topRight}</p>
      <p className={fixedClasses}>{value.bottomLeft}</p>
      <p className={adaptableClasses}>{duration}</p>
      {hasActions && (
        <>
          <Button
            className={`${actionClasses} col-start-7 sm:col-start-9 lg:col-start-11`}
            color="primary"
            onPress={handleEdit}
          >
            <FaPen size={15} />
          </Button>
          <Button
            className={`${actionClasses}`}
            color="danger"
            onPress={handleDelete}
          >
            <FaTrash size={15} />
          </Button>
        </>
      )}
      {value.description && (
        <pre className={`col-span-12 whitespace-break-spaces ${!hasActions ? 'mt-5' : ''}`}>
          {value.description}
        </pre>
      )}
    </div>
  )
}

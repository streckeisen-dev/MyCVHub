import { PageInfo } from '@/types/Page.ts'
import { ApplicationSearchDto } from '@/types/application/ApplicationSearchDto.ts'
import {
  Button,
  Chip,
  Input,
  Pagination,
  Select,
  SelectItem,
  SharedSelection,
  SortDescriptor,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableColumn,
  TableHeader,
  TableRow,
  Tooltip
} from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { ReactNode, RefObject, useCallback, useEffect, useImperativeHandle, useState } from 'react'
import { Key } from '@react-types/shared'
import { formatDateTime } from '@/helpers/DateHelper.ts'
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'
import { useAsyncList } from '@react-stately/data'
import ApplicationApi, { ApplicationSearchRequest } from '@/api/ApplicationApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { FaSearch } from 'react-icons/fa'
import debounce from 'lodash.debounce'
import { DeleteApplicationButton } from '@/components/application/DeleteApplicationButton.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { useNavigate } from 'react-router-dom'
import { ApplicationStatus } from '@/components/application/ApplicationStatus.tsx'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'
import { ArchiveApplicationButton } from '@/components/application/ArchiveApplicationButton.tsx'
import { TableButton } from '@/components/TableButton.tsx'
import { FaArrowRotateLeft, FaEye } from 'react-icons/fa6'
import ApplicationFilterService from '@/components/application/ApplicationFilterService.ts'

const ROWS_PER_PAGE_OPTIONS = ['5', '10', '25']

const COLUMNS = [
  {
    key: 'jobTitle',
    labelKey: 'fields.jobTitle'
  },
  {
    key: 'company',
    labelKey: 'fields.company'
  },
  {
    key: 'status',
    labelKey: 'fields.status'
  },
  {
    key: 'createdAt',
    labelKey: 'application.createdAt'
  },
  {
    key: 'updatedAt',
    labelKey: 'application.updatedAt'
  },
  {
    key: 'actions',
    labelKey: 'table.actions',
    sortable: false
  }
]

export interface ApplicationTableControls {
  refresh: () => void
}

export type ApplicationTableProps = Readonly<{
  statuses: ApplicationStatusDto[]
  ref: RefObject<ApplicationTableControls | null>
}>

interface ApplicationAsyncListValue {
  items: ApplicationSearchDto[]
}

export function ApplicationTable(props: ApplicationTableProps) {
  const { statuses, ref } = props
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()

  const [page, setPage] = useState<number>(0)
  const [pageInfo, setPageInfo] = useState<PageInfo>()
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [statusFilter, setStatusFilter] = useState<string | undefined>(
    ApplicationFilterService.getApplicationStatusFilter()
  )
  const [searchTerm, setSearchTerm] = useState<string | undefined>(
    ApplicationFilterService.getApplicationSearchTermFilter
  )
  const [pageSize, setPageSize] = useState<string>(
    ApplicationFilterService.getPageSize() ?? ROWS_PER_PAGE_OPTIONS[0]
  )
  const [includeArchivedFilter, setIncludeArchivedFilter] = useState<boolean>(
    ApplicationFilterService.getIncludeArchivedFilter() ?? false
  )

  const applications = useAsyncList<ApplicationSearchDto>({
    async load({ signal }): Promise<ApplicationAsyncListValue> {
      return await loadApplications(signal, applications.sortDescriptor)
    },
    async sort({ signal, sortDescriptor }): Promise<ApplicationAsyncListValue> {
      return await loadApplications(signal, sortDescriptor)
    }
  })

  useImperativeHandle(ref, () => {
    return {
      refresh: applications.reload
    }
  })

  async function loadApplications(
    signal: AbortSignal,
    sort?: SortDescriptor
  ): Promise<ApplicationAsyncListValue> {
    setIsLoading(true)

    const searchRequest: ApplicationSearchRequest = {
      page,
      searchTerm,
      status: statusFilter,
      includeArchived: includeArchivedFilter,
      sort,
      pageSize
    }
    try {
      const result = await ApplicationApi.search(
        searchRequest,
        i18n.language,
        signal
      )
      setPageInfo(result.page)
      return {
        items: result.content
      }
    } catch (e) {
      if (!signal.aborted) {
        const error = (e as RestError).errorDto
        addErrorToast(t('application.loadingError'), error?.message ?? t('error.genericMessage'))
      }
      return { items: [] }
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    applications.reload()
  }, [statusFilter, searchTerm, page, pageSize, includeArchivedFilter])

  async function onPageChange(pageNumber: number) {
    setPage(pageNumber - 1)
  }

  function handleStatusFilterChange(keys: SharedSelection) {
    setPageInfo(undefined)
    setStatusFilter(keys.currentKey ?? undefined)
    ApplicationFilterService.setApplicationStatusFilter(keys.currentKey)
  }

  const debouncedSetSearch = useCallback(
    debounce((value: string) => setSearchTerm(value), 600),
    []
  )

  function handleSearchTermChange(value: string) {
    setPageInfo(undefined)
    debouncedSetSearch(value)
    ApplicationFilterService.setApplicationSearchTermFilter(value)
  }

  function handlePageSizeChange(keys: SharedSelection) {
    setPageInfo(undefined)
    setPageSize(keys.currentKey ?? ROWS_PER_PAGE_OPTIONS[0])
    ApplicationFilterService.setPageSize(keys.currentKey)
  }

  function handleArchivedFilterChange(includeArchived: boolean) {
    setIncludeArchivedFilter(includeArchived)
    ApplicationFilterService.setIncludeArchivedFilter(includeArchived)
  }

  function handleView(id: number) {
    navigate(getRoutePath(RouteId.ApplicationDetail, undefined, id.toString()))
  }

  function handleUpdate() {
    applications.reload()
  }

  function handleFilterReset() {
    handleSearchTermChange('')
    handleStatusFilterChange({ currentKey: undefined } as SharedSelection)
    handlePageSizeChange({ currentKey: ROWS_PER_PAGE_OPTIONS[0] } as SharedSelection)
    handleArchivedFilterChange(false)
  }

  function renderCell(application: ApplicationSearchDto, columnKey: Key): ReactNode {
    const cellValue = application[columnKey as keyof ApplicationSearchDto] as unknown
    switch (columnKey) {
      case 'status': {
        const status = cellValue as ApplicationStatusDto
        return (
          <div className="flex flex-row flex-wrap gap-2">
            <ApplicationStatus status={status} />
            {application.isArchived && <Chip color="warning">{t('application.archived')}</Chip>}
          </div>
        )
      }
      case 'actions': {
        return (
          <div className="flex f lex-wrap flex-row gap-2">
            <Tooltip color="primary" content={t('application.view')}>
              <TableButton className="text-primary" onClick={() => handleView(application.id)}>
                <FaEye />
              </TableButton>
            </Tooltip>
            {application.status.isTerminal && !application.isArchived && (
              <ArchiveApplicationButton id={application.id} onArchive={handleUpdate} />
            )}
            <DeleteApplicationButton id={application.id} onDelete={handleUpdate} />
          </div>
        )
      }
      case 'createdAt':
      case 'updatedAt': {
        const date = cellValue as string
        if (cellValue) {
          return <span>{formatDateTime(date)}</span>
        }
        return <span>&mdash;</span>
      }
      default:
        return <span>{cellValue as string}</span>
    }
  }

  const pages = pageInfo?.totalPages ?? 0
  const pageStart = (pageInfo?.number ?? 0) * (pageInfo?.size ?? 0)

  return (
    <Table
      topContent={
        <div className="flex flex-col items-start gap-2">
          <Button variant="light" startContent={<FaArrowRotateLeft />} onPress={handleFilterReset}>
            {t('table.resetFilter')}
          </Button>
          <div className="w-full flex flex-wrap gap-6 sm:items-end">
            <Input
              className="w-60"
              name="search"
              label={t('table.search')}
              onValueChange={handleSearchTermChange}
              startContent={<FaSearch />}
              isClearable
            />
            {statuses.length > 0 && (
              <Select
                className="w-65"
                items={statuses}
                name="status"
                label={t('fields.status')}
                selectedKeys={statusFilter ? [statusFilter] : []}
                onSelectionChange={handleStatusFilterChange}
                isClearable
              >
                {(status) => <SelectItem key={status.key}>{status.name}</SelectItem>}
              </Select>
            )}

            <Select
              className="w-40"
              name="pageSize"
              label={t('table.pagination.pageSize')}
              selectedKeys={[pageSize]}
              onSelectionChange={handlePageSizeChange}
            >
              {ROWS_PER_PAGE_OPTIONS.map((option) => (
                <SelectItem key={option}>{option}</SelectItem>
              ))}
            </Select>

            <CheckboxInput
              label={t('application.includeArchived')}
              isSelected={includeArchivedFilter}
              onValueChange={handleArchivedFilterChange}
            />

            <p className="text-default-500 grow text-right self-end">
              {t('table.pagination.numberInformation', {
                start: applications.items.length === 0 ? 0 : pageStart + 1,
                end: pageStart + applications.items.length,
                total: pageInfo?.totalElements ?? 0
              })}
            </p>
          </div>
        </div>
      }
      bottomContent={
        pageInfo &&
        pages > 0 && (
          <div className="flex justify-center">
            <Pagination
              isCompact
              showControls
              showShadow
              page={page + 1}
              total={pages}
              onChange={onPageChange}
            />
          </div>
        )
      }
      sortDescriptor={applications.sortDescriptor}
      onSortChange={applications.sort}
    >
      <TableHeader columns={COLUMNS}>
        {(column) => (
          <TableColumn key={column.key} allowsSorting={column.sortable ?? true}>
            {t(column.labelKey)}
          </TableColumn>
        )}
      </TableHeader>
      <TableBody
        isLoading={isLoading}
        items={applications.items}
        emptyContent={t('application.noEntries')}
        loadingContent={<Spinner />}
      >
        {(application) => {
          return (
            <TableRow key={application.id} className={''}>
              {(column) => <TableCell key={column}>{renderCell(application, column)}</TableCell>}
            </TableRow>
          )
        }}
      </TableBody>
    </Table>
  )
}

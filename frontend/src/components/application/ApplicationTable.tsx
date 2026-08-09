import { Chip } from '@/components/ui/Display.tsx'
import { Tooltip } from '@/components/ui/Tooltip.tsx'
import { Button } from '@/components/ui/Button.tsx'
import { Input } from '@/components/ui/Fields.tsx'
import { PageInfo } from '@/types/Page.ts'
import { ApplicationSearchDto } from '@/types/application/ApplicationSearchDto.ts'
import { Label, ListBox, Pagination, Select, Spinner, Table } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { MouseEvent, PointerEvent, ReactNode, RefObject, useCallback, useEffect, useImperativeHandle, useState } from 'react'
import { Key, SortDescriptor } from '@react-types/shared'
import { formatDateTime } from '@/helpers/DateHelper.ts'
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'
import { useAsyncList } from '@react-stately/data'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import debounce from 'lodash.debounce'
import { DeleteApplicationButton } from '@/components/application/DeleteApplicationButton.tsx'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { useNavigate } from 'react-router-dom'
import { ApplicationStatus } from '@/components/application/ApplicationStatus.tsx'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'
import { ArchiveApplicationButton } from '@/components/application/ArchiveApplicationButton.tsx'
import { TableButton } from '@/components/btn/TableButton.tsx'
import { FaArrowRotateLeft, FaEye, FaXmark } from 'react-icons/fa6'
import ApplicationFilterService from '@/components/application/ApplicationFilterService.ts'
import { ApplicationSearchRequest } from '@/types/application/ApplicationSearchRequest.ts'

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

function stopStatusFilterClearPress(event: MouseEvent | PointerEvent) {
  event.preventDefault()
  event.stopPropagation()
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
  const [searchInputValue, setSearchInputValue] = useState<string>(
    ApplicationFilterService.getApplicationSearchTermFilter() ?? ''
  )
  const [searchTerm, setSearchTerm] = useState<string | undefined>(
    ApplicationFilterService.getApplicationSearchTermFilter()
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
      const result = await ApplicationApi.search(searchRequest, i18n.language, signal)
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

  function handleStatusFilterChange(key: Key | null) {
    const value = key == null ? undefined : String(key)
    setPage(0)
    setStatusFilter(value)
    ApplicationFilterService.setApplicationStatusFilter(value)
  }

  function handleStatusFilterClear(event: MouseEvent<HTMLButtonElement>) {
    event.preventDefault()
    event.stopPropagation()
    handleStatusFilterChange(null)
  }

  const debouncedSetSearch = useCallback(
    debounce((value: string) => setSearchTerm(value), 600),
    []
  )

  function handleSearchTermChange(value: string) {
    setSearchInputValue(value)
    setPage(0)
    debouncedSetSearch(value)
    ApplicationFilterService.setApplicationSearchTermFilter(value)
  }

  function handlePageSizeChange(key: Key | null) {
    const value = key == null ? ROWS_PER_PAGE_OPTIONS[0] : String(key)
    setPage(0)
    setPageSize(value)
    ApplicationFilterService.setPageSize(value)
  }

  function handleArchivedFilterChange(includeArchived: boolean) {
    setPage(0)
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
    handleStatusFilterChange(null)
    handlePageSizeChange(ROWS_PER_PAGE_OPTIONS[0])
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
          <div className="flex flex-wrap flex-row gap-2">
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

  const pages = Math.max(pageInfo?.totalPages ?? 1, 1)
  const pageStart = (pageInfo?.number ?? 0) * (pageInfo?.size ?? 0)

  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-col items-start gap-3">
        <Button variant="tertiary" onPress={handleFilterReset}>
          <FaArrowRotateLeft />
          {t('table.resetFilter')}
        </Button>
        <div className="w-full flex flex-wrap gap-5 sm:items-end">
          <Input
            className="min-w-60 flex-1 sm:max-w-80"
            name="search"
            label={t('table.search')}
            value={searchInputValue}
            onValueChange={handleSearchTermChange}
          />
          {statuses.length > 0 && (
            <Select
              className="w-65"
              name="status"
              value={statusFilter ?? null}
              onChange={handleStatusFilterChange}
            >
              <Label>{t('fields.status')}</Label>
              <Select.Trigger>
                <Select.Value />
                {statusFilter && (
                  <button
                    aria-label={t('table.resetFilter')}
                    className="inline-flex h-6 w-6 items-center justify-center rounded-full text-default-500 transition-colors hover:bg-default/10 hover:text-foreground"
                    type="button"
                    onMouseDown={stopStatusFilterClearPress}
                    onPointerDown={stopStatusFilterClearPress}
                    onClick={handleStatusFilterClear}
                  >
                    <FaXmark size={14} />
                  </button>
                )}
                <Select.Indicator />
              </Select.Trigger>
              <Select.Popover>
                <ListBox>
                  {statuses.map((status) => (
                    <ListBox.Item key={status.key} id={status.key} textValue={status.name}>
                      {status.name}
                      <ListBox.ItemIndicator />
                    </ListBox.Item>
                  ))}
                </ListBox>
              </Select.Popover>
            </Select>
          )}

          <Select
            className="w-40 min-w-40"
            name="pageSize"
            value={pageSize}
            onChange={handlePageSizeChange}
          >
            <Label>{t('table.pagination.pageSize')}</Label>
            <Select.Trigger>
              <Select.Value />
              <Select.Indicator />
            </Select.Trigger>
            <Select.Popover>
              <ListBox>
                {ROWS_PER_PAGE_OPTIONS.map((option) => (
                  <ListBox.Item key={option} id={option} textValue={option}>
                    {option}
                    <ListBox.ItemIndicator />
                  </ListBox.Item>
                ))}
              </ListBox>
            </Select.Popover>
          </Select>

          <div className="pb-2">
            <CheckboxInput
              label={t('application.includeArchived')}
              isSelected={includeArchivedFilter}
              onValueChange={handleArchivedFilterChange}
            />
          </div>

          <p className="text-default-500 ml-auto pb-2 text-right">
            {t('table.pagination.numberInformation', {
              start: applications.items.length === 0 ? 0 : pageStart + 1,
              end: pageStart + applications.items.length,
              total: pageInfo?.totalElements ?? 0
            })}
          </p>
        </div>
      </div>
      <Table className="app-table w-full">
        <Table.ScrollContainer>
          <Table.Content
            aria-label={t('application.title')}
            sortDescriptor={applications.sortDescriptor}
            onSortChange={applications.sort}
          >
            <Table.Header>
              {COLUMNS.map((column) => (
                <Table.Column
                  key={column.key}
                  id={column.key}
                  allowsSorting={column.sortable ?? true}
                >
                  {t(column.labelKey)}
                </Table.Column>
              ))}
            </Table.Header>
            <Table.Body
              renderEmptyState={() => (isLoading ? <Spinner /> : t('application.noEntries'))}
            >
              {!isLoading &&
                applications.items.map((application) => (
                  <Table.Row key={application.id}>
                    {COLUMNS.map((column) => (
                      <Table.Cell key={column.key}>
                        {renderCell(application, column.key)}
                      </Table.Cell>
                    ))}
                  </Table.Row>
                ))}
            </Table.Body>
          </Table.Content>
        </Table.ScrollContainer>
        {pageInfo && (
          <Table.Footer>
            <div className="flex justify-center">
              <Pagination>
                <Pagination.Content>
                  <Pagination.Item>
                    <Pagination.Previous isDisabled={page <= 0} onPress={() => onPageChange(page)}>
                      {t('table.pagination.previous')}
                    </Pagination.Previous>
                  </Pagination.Item>
                  <Pagination.Item>
                    {page + 1} / {pages}
                  </Pagination.Item>
                  <Pagination.Item>
                    <Pagination.Next
                      isDisabled={page + 1 >= pages}
                      onPress={() => onPageChange(page + 2)}
                    >
                      {t('table.pagination.next')}
                    </Pagination.Next>
                  </Pagination.Item>
                </Pagination.Content>
              </Pagination>
            </div>
          </Table.Footer>
        )}
      </Table>
    </div>
  )
}

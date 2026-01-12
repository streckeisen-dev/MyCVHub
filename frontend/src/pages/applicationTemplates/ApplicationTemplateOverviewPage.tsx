import { ReactNode, useEffect, useState } from 'react'
import { centerSection, h1 } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import {
  Button,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableColumn,
  TableHeader,
  TableRow,
  Tooltip
} from '@heroui/react'
import { ColumnSize } from '@react-types/table'
import { Key } from '@react-types/shared'
import { TableButton } from '@/components/TableButton.tsx'
import { FaEye, FaTrash } from 'react-icons/fa6'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { useNavigate } from 'react-router-dom'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { RestError } from '@/types/RestError.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { DeleteApplicationTemplateModal } from '@/components/applicationTemplate/DeleteApplicationTemplateModal.tsx'

interface ColumnDefinition {
  key: string
  labelKey: string
  width: ColumnSize
}

const COLUMNS: ColumnDefinition[] = [
  {
    key: 'name',
    labelKey: 'fields.name',
    width: '80%'
  },
  {
    key: 'actions',
    labelKey: 'table.actions',
    width: '20%'
  }
]

export function ApplicationTemplateOverviewPage(): ReactNode {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()

  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [templates, setTemplates] = useState<ApplicationTemplateDto[]>([])
  const [profile, setProfile] = useState<ProfileDto>()

  useEffect(() => {
    async function loadTemplates() {
      try {
        const templates = await ApplicationTemplateApi.getApplicationTemplates(i18n.language)
        setTemplates(templates)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(
          t('applicationTemplate.loadingError'),
          error?.message ?? t('error.genericMessage')
        )
      }

      try {
        const result = await ProfileApi.getProfile(i18n.language)
        setProfile(result)
      } catch (e) {
        const error = (e as RestError).errorDto
        addErrorToast(t('profile.loadingError'), error?.message ?? t('error.genericMessage'))
      } finally {
        setIsLoading(false)
      }
    }
    loadTemplates()
  }, [])

  function handleView(id: number) {
    navigate(getRoutePath(RouteId.ApplicationTemplateDetails, undefined, id.toString()))
  }

  function handleAdd() {
    navigate(getRoutePath(RouteId.AddApplicationTemplate))
  }

  function handleDelete(id: number) {
    setTemplates(prev => {
      return [...prev.filter(template => template.id !== id)]
    })
  }

  function renderCell(column: Key, template: ApplicationTemplateDto): ReactNode {
    switch (column) {
      case 'name':
        return <p>{template.name}</p>
      case 'actions':
        return (
          <div className="flex gap-2 items-center">
            <Tooltip color="primary" content={t('applicationTemplate.view')}>
              <TableButton className="text-primary" onClick={() => handleView(template.id)}>
                <FaEye />
              </TableButton>
            </Tooltip>
            <DeleteApplicationTemplateModal
              id={template.id}
              trigger={
                <Tooltip color="danger" content={t('applicationTemplate.delete.title')}>
                  <TableButton className="text-danger">
                    <FaTrash />
                  </TableButton>
                </Tooltip>
              }
              onDelete={() => handleDelete(template.id)}
            />
          </div>
        )
    }
  }

  return (
    <section className={centerSection()}>
      <h1 className={h1()}>{t('applicationTemplate.title')}</h1>

      {profile && (
        <div className="mt-5 w-full flex flex-col items-end">
          <Button color="primary" onPress={handleAdd}>
            {t('applicationTemplate.editor.add')}
          </Button>
        </div>
      )}

      <Table className="mt-5">
        <TableHeader columns={COLUMNS}>
          {(column) => (
            <TableColumn key={column.key} width={column.width}>
              {t(column.labelKey)}
            </TableColumn>
          )}
        </TableHeader>
        <TableBody
          isLoading={isLoading}
          items={templates}
          emptyContent={t('applicationTemplate.noEntries')}
          loadingContent={<Spinner />}
        >
          {(template) => (
            <TableRow key={template.id}>
              {(column) => <TableCell>{renderCell(column, template)}</TableCell>}
            </TableRow>
          )}
        </TableBody>
      </Table>
    </section>
  )
}

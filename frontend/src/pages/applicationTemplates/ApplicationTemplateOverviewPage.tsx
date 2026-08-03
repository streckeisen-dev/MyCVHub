import { Button } from '@/components/ui/Button.tsx'
import { ReactNode, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Spinner, Table } from '@heroui/react'
import { ColumnSize } from '@react-types/table'
import { Key } from '@react-types/shared'
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
import { TooltipButton } from '@/components/btn/TooltipButton.tsx'
import { Page, PageHeader, PageTitle } from '@/components/ui/Layout.tsx'

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
    setTemplates((prev) => {
      return prev.filter((template) => template.id !== id)
    })
  }

  function renderCell(column: Key, template: ApplicationTemplateDto): ReactNode {
    switch (column) {
      case 'name':
        return <p>{template.name}</p>
      case 'actions':
        return (
          <div className="flex gap-2 items-center">
            <TooltipButton
              color="primary"
              content={t('applicationTemplate.view')}
              onClick={() => handleView(template.id)}
            >
              <FaEye />
            </TooltipButton>
            <DeleteApplicationTemplateModal
              id={template.id}
              trigger={
                <TooltipButton color="danger" content={t('applicationTemplate.delete.title')}>
                  <FaTrash />
                </TooltipButton>
              }
              onDelete={() => handleDelete(template.id)}
            />
          </div>
        )
    }
  }

  const showTable = isLoading || templates.length > 0

  return (
    <Page size="wide">
      <PageHeader className="sm:flex-row sm:items-end sm:justify-between">
        <PageTitle>{t('applicationTemplate.title')}</PageTitle>

        {profile && (
          <Button variant="primary" onPress={handleAdd}>
            {t('applicationTemplate.editor.add')}
          </Button>
        )}
      </PageHeader>

      {showTable ? (
        <Table className="app-table w-full">
          <Table.ScrollContainer>
            <Table.Content aria-label={t('applicationTemplate.title')}>
              <Table.Header>
                {COLUMNS.map((column) => (
                  <Table.Column key={column.key} id={column.key} width={column.width}>
                    {t(column.labelKey)}
                  </Table.Column>
                ))}
              </Table.Header>
              <Table.Body renderEmptyState={() => <Spinner />}>
                {!isLoading &&
                  templates.map((template) => (
                    <Table.Row key={template.id}>
                      {COLUMNS.map((column) => (
                        <Table.Cell key={column.key}>{renderCell(column.key, template)}</Table.Cell>
                      ))}
                    </Table.Row>
                  ))}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table>
      ) : (
        <div className="flex min-h-48 w-full flex-col items-center justify-center rounded-lg border border-dashed border-default-300 bg-default-50/60 px-6 py-10 text-center">
          <p className="max-w-md text-sm leading-6 text-default-600">
            {t('applicationTemplate.noEntries')}
          </p>
        </div>
      )}
    </Page>
  )
}

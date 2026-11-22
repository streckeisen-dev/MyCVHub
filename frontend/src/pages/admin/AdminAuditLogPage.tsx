import { fetchFromAdminApi } from '@/api/AdminAuthApi.ts'
import { getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { Button } from '@/components/ui/Button.tsx'
import { Page, PageTitle } from '@/components/ui/Layout.tsx'
import { formatAdminDateTime } from '@/helpers/AdminDateTime.ts'
import { Spinner, Table } from '@heroui/react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

type AuditEvent = { id: number; timestamp: string; actor: string | null; actorSource: string; action: string; result: string; targetType: string | null; targetId: string | null }
type AuditPage = { content: AuditEvent[]; page: { totalPages: number } }

export function AdminAuditLogPage() {
  const { t, i18n } = useTranslation()
  const [page, setPage] = useState(0)
  const [data, setData] = useState<AuditPage>()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const response = await fetchFromAdminApi(`/admin/audit-log?page=${page}`, i18n.language)
        const result = await getJSONIfResponseIsOk<AuditPage>(response)
        if (!cancelled) { setData(result); setError(false) }
      } catch { if (!cancelled) setError(true) } finally { if (!cancelled) setLoading(false) }
    }
    void load()
    return () => { cancelled = true }
  }, [page, i18n.language, revision])
  return <Page size="wide" className="px-4 sm:px-6">
    <PageTitle>{t('admin.audit.title')}</PageTitle>
    {error && <p role="alert">{t('admin.audit.error')} <Button onPress={() => setRevision(value => value + 1)}>{t('admin.accounts.retry')}</Button></p>}
    {loading ? <Spinner aria-label={t('admin.accounts.loading')} /> : !error && <>
      <Table className="app-table w-full"><Table.ScrollContainer><Table.Content aria-label={t('admin.audit.title')}>
        <Table.Header><Table.Column isRowHeader>{t('admin.audit.time')}</Table.Column><Table.Column>{t('admin.audit.actor')}</Table.Column><Table.Column>{t('admin.audit.action')}</Table.Column><Table.Column>{t('admin.audit.result')}</Table.Column><Table.Column>{t('admin.audit.target')}</Table.Column></Table.Header>
        <Table.Body renderEmptyState={() => t('admin.audit.empty')}>{data?.content.map(event => <Table.Row key={event.id}><Table.Cell>{formatAdminDateTime(event.timestamp, i18n.language)}</Table.Cell><Table.Cell>{event.actor ?? event.actorSource}</Table.Cell><Table.Cell>{event.action}</Table.Cell><Table.Cell>{event.result}</Table.Cell><Table.Cell>{[event.targetType, event.targetId].filter(Boolean).join(': ') || '—'}</Table.Cell></Table.Row>)}</Table.Body>
      </Table.Content></Table.ScrollContainer></Table>
      <div className="flex items-center gap-3"><Button isDisabled={page === 0} onPress={() => setPage(value => value - 1)}>{t('admin.users.previous')}</Button><span>{page + 1} / {Math.max(1, data?.page.totalPages ?? 1)}</span><Button isDisabled={page + 1 >= (data?.page.totalPages ?? 0)} onPress={() => setPage(value => value + 1)}>{t('admin.users.next')}</Button></div>
    </>}
  </Page>
}

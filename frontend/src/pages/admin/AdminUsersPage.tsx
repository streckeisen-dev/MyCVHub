import { fetchFromAdminApi } from '@/api/AdminAuthApi.ts'
import { getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { Button } from '@/components/ui/Button.tsx'
import { Input } from '@/components/ui/Fields.tsx'
import { Spinner, Table } from '@heroui/react'
import { Page, PageTitle } from '@/components/ui/Layout.tsx'
import { formatAdminDateTime } from '@/helpers/AdminDateTime.ts'
import { FormEvent, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

type User = { username: string; email: string | null; createdAt: string | null; lastLoginAt: string | null }
type UsersPage = { content: User[]; page: { number: number; totalPages: number } }

export function AdminUsersPage() {
  const { t, i18n } = useTranslation()
  const [input, setInput] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<UsersPage>()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const response = await fetchFromAdminApi(`/admin/users?search=${encodeURIComponent(search)}&page=${page}`, i18n.language)
        const result = await getJSONIfResponseIsOk<UsersPage>(response)
        if (!cancelled) { setData(result); setError(false) }
      } catch { if (!cancelled) setError(true) } finally { if (!cancelled) setLoading(false) }
    }
    void load()
    return () => { cancelled = true }
  }, [search, page, i18n.language, revision])
  function submit(e: FormEvent) { e.preventDefault(); setPage(0); setSearch(input) }
  const date = (value: string | null) => value ? formatAdminDateTime(value, i18n.language) : t('admin.users.unknown')
  return <Page size="wide" className="px-4 sm:px-6">
    <PageTitle>{t('admin.users.title')}</PageTitle>
    <form onSubmit={submit} className="flex flex-wrap items-end gap-3"><Input className="max-w-sm" label={t('admin.users.search')} value={input} onValueChange={setInput} /><Button type="submit">{t('admin.users.search')}</Button></form>
    {error && <p role="alert">{t('admin.users.error')} <Button onPress={() => setRevision(v => v + 1)}>{t('admin.accounts.retry')}</Button></p>}
    {loading ? <Spinner aria-label={t('admin.accounts.loading')} /> : !error && <><Table className="app-table w-full"><Table.ScrollContainer><Table.Content aria-label={t('admin.users.title')}><Table.Header><Table.Column isRowHeader>{t('admin.users.username')}</Table.Column><Table.Column>{t('admin.users.email')}</Table.Column><Table.Column>{t('admin.users.lastLogin')}</Table.Column><Table.Column>{t('admin.users.created')}</Table.Column></Table.Header><Table.Body renderEmptyState={() => t('admin.users.empty')}>{data?.content.map(user => <Table.Row key={user.username}><Table.Cell>{user.username}</Table.Cell><Table.Cell>{user.email ?? t('admin.users.unknown')}</Table.Cell><Table.Cell>{date(user.lastLoginAt)}</Table.Cell><Table.Cell>{date(user.createdAt)}</Table.Cell></Table.Row>)}</Table.Body></Table.Content></Table.ScrollContainer></Table><div className="flex items-center gap-3"><Button isDisabled={page === 0} onPress={() => setPage(p => p - 1)}>{t('admin.users.previous')}</Button><span>{page + 1} / {Math.max(1, data?.page.totalPages ?? 1)}</span><Button isDisabled={page + 1 >= (data?.page.totalPages ?? 0)} onPress={() => setPage(p => p + 1)}>{t('admin.users.next')}</Button></div></>}
  </Page>
}

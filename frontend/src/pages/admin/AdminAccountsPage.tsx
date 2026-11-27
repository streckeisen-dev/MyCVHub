import { fetchFromAdminApi } from '@/api/AdminAuthApi.ts'
import { extractErrorIfResponseIsNotOk, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { Button } from '@/components/ui/Button.tsx'
import { Input } from '@/components/ui/Fields.tsx'
import { Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@/components/ui/Modal.tsx'
import { Spinner, Table } from '@heroui/react'
import { Page, PageTitle } from '@/components/ui/Layout.tsx'
import { formatAdminDateTime } from '@/helpers/AdminDateTime.ts'
import { FormEvent, useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

type Account = { id: number; username: string; active: boolean }
type CreatedAccount = { username: string; temporaryPassword: string; expiresAt: string }

export function AdminAccountsPage() {
  const { t, i18n } = useTranslation()
  const [accounts, setAccounts] = useState<Account[]>([])
  const [username, setUsername] = useState('')
  const [created, setCreated] = useState<CreatedAccount>()
  const [error, setError] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [confirmation, setConfirmation] = useState<{ account: Account; action: 'reset' | 'deactivate' | 'delete' }>()
  const load = useCallback(async () => {
    setLoading(true)
    try {
      const response = await fetchFromAdminApi('/admin/accounts', i18n.language)
      setAccounts(await getJSONIfResponseIsOk<Account[]>(response))
      setError(false)
    } catch { setError(true) } finally { setLoading(false) }
  }, [i18n.language])
  useEffect(() => { void load() }, [load])
  async function create(e: FormEvent) {
    e.preventDefault()
    if (created) return
    setBusy(true)
    try {
      const response = await fetchFromAdminApi('/admin/accounts', i18n.language, { method: 'POST', body: JSON.stringify({ username }) })
      setCreated(await getJSONIfResponseIsOk<CreatedAccount>(response))
      setUsername('')
      await load()
    } catch { setError(true) } finally { setBusy(false) }
  }
  async function deactivate(id: number) {
    setBusy(true)
    try {
      await extractErrorIfResponseIsNotOk(await fetchFromAdminApi(`/admin/accounts/${id}/deactivate`, i18n.language, { method: 'POST' }))
      await load()
    } catch { setError(true) } finally { setBusy(false) }
  }
  async function resetPassword(id: number) {
    if (created) return
    setBusy(true)
    try {
      const response = await fetchFromAdminApi(`/admin/accounts/${id}/reset-password`, i18n.language, { method: 'POST' })
      setCreated(await getJSONIfResponseIsOk<CreatedAccount>(response))
      setError(false)
    } catch { setError(true) } finally { setBusy(false) }
  }
  async function deleteAccount(id: number) {
    setBusy(true)
    try {
      await extractErrorIfResponseIsNotOk(await fetchFromAdminApi(`/admin/accounts/${id}`, i18n.language, { method: 'DELETE' }))
      await load()
    } catch { setError(true) } finally { setBusy(false) }
  }
  async function confirmAction() {
    if (!confirmation) return
    const { account, action } = confirmation
    if (action === 'reset') await resetPassword(account.id)
    else if (action === 'deactivate') await deactivate(account.id)
    else await deleteAccount(account.id)
    setConfirmation(undefined)
  }
  return <Page size="wide" className="px-4 sm:px-6">
    <PageTitle>{t('admin.accounts.title')}</PageTitle>
    <form onSubmit={create} className="flex flex-wrap items-end gap-3"><Input className="max-w-sm" label={t('admin.accounts.username')} isRequired maxLength={100} value={username} onValueChange={setUsername} /><Button type="submit" isPending={busy} isDisabled={!!created}>{t('admin.accounts.create')}</Button></form>
    {created && <div role="status" className="border border-warning p-4"><p>{t('admin.accounts.temporaryPassword', { username: created.username })}</p><code className="break-all select-all">{created.temporaryPassword}</code><p>{t('admin.accounts.expires', { value: formatAdminDateTime(created.expiresAt, i18n.language), interpolation: { escapeValue: false } })}</p><Button onPress={() => setCreated(undefined)}>{t('admin.accounts.dismiss')}</Button></div>}
    {error && <p role="alert">{t('admin.accounts.error')} <Button onPress={() => { void load() }}>{t('admin.accounts.retry')}</Button></p>}
    {loading ? <Spinner aria-label={t('admin.accounts.loading')} /> : <Table className="app-table w-full"><Table.ScrollContainer><Table.Content aria-label={t('admin.accounts.title')}><Table.Header><Table.Column isRowHeader>{t('admin.accounts.username')}</Table.Column><Table.Column>{t('admin.accounts.status')}</Table.Column><Table.Column>{t('admin.accounts.actions')}</Table.Column></Table.Header><Table.Body renderEmptyState={() => t('admin.accounts.empty')}>{accounts.map(account => <Table.Row key={account.id}><Table.Cell>{account.username}</Table.Cell><Table.Cell>{t(account.active ? 'admin.accounts.active' : 'admin.accounts.inactive')}</Table.Cell><Table.Cell><div className="flex flex-wrap gap-2">{account.active && <><Button isDisabled={busy} onPress={() => setConfirmation({ account, action: 'reset' })}>{t('admin.accounts.reset')}</Button><Button isDisabled={busy} onPress={() => setConfirmation({ account, action: 'deactivate' })}>{t('admin.accounts.deactivate')}</Button></>}<Button variant="danger" isDisabled={busy} onPress={() => setConfirmation({ account, action: 'delete' })}>{t('admin.accounts.delete')}</Button></div></Table.Cell></Table.Row>)}</Table.Body></Table.Content></Table.ScrollContainer></Table>}
    <Modal isOpen={!!confirmation} onOpenChange={open => { if (!open) setConfirmation(undefined) }}><ModalContent><ModalHeader>{confirmation && t(`admin.accounts.${confirmation.action}`)}</ModalHeader><ModalBody>{confirmation && t(`admin.accounts.confirm${confirmation.action === 'reset' ? 'Reset' : confirmation.action === 'deactivate' ? 'Deactivate' : 'Delete'}`, { username: confirmation.account.username, interpolation: { escapeValue: false } })}</ModalBody><ModalFooter><Button variant="tertiary" onPress={() => setConfirmation(undefined)}>{t('forms.cancel')}</Button><Button variant={confirmation?.action === 'delete' ? 'danger' : 'primary'} isPending={busy} onPress={() => { void confirmAction() }}>{confirmation && t(`admin.accounts.${confirmation.action}`)}</Button></ModalFooter></ModalContent></Modal>
  </Page>
}

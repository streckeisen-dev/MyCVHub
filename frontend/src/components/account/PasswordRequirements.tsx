import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { PasswordFormState } from '@/components/account/PasswordForm.tsx'
import { FaCheck, FaXmark } from 'react-icons/fa6'

export type PasswordRequirementsProps = Readonly<{
  state: PasswordFormState
}>

export function PasswordRequirements(props: PasswordRequirementsProps): ReactNode {
  const { t } = useTranslation()
  const { state } = props
  const minPasswordLength = '8'

  const passwordRequirements = [
    {
      key: 'length',
      name: t('passwordRequirements.length')
        .replace('{length}', minPasswordLength)
        .replace('{{}}', minPasswordLength),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        return state.password.length >= Number(minPasswordLength)
      }
    },
    {
      key: 'whitespaces',
      name: t('passwordRequirements.whitespaces'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password
        return pw != '' && !pw.includes(' ')
      }
    },
    {
      key: 'digits',
      name: t('passwordRequirements.digits'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        return /\d/.test(state.password)
      }
    },
    {
      key: 'specialChars',
      name: t('passwordRequirements.specialChars'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        return /\W/.test(state.password)
      }
    },
    {
      key: 'uppercase',
      name: t('passwordRequirements.uppercase'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password
        return pw.toLowerCase() !== pw
      }
    },
    {
      key: 'lowercase',
      name: t('passwordRequirements.lowercase'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password
        return pw.toUpperCase() !== pw
      }
    },
    {
      key: 'match',
      name: t('passwordRequirements.match'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        return state.password === state.confirmPassword
      }
    }
  ]

  return (
    <div className="rounded-lg border border-default-200 bg-surface p-5">
      <h4 className="text-base font-semibold text-foreground">
        {t('account.passwordRequirements')}
      </h4>
      <div className="mt-4 grid gap-2 sm:grid-cols-2">
        {passwordRequirements.map((requirement) => {
          const fulfilled = requirement.predicate()
          const Icon = fulfilled ? FaCheck : FaXmark
          return (
            <span
              key={requirement.key}
              className="inline-flex items-start gap-2 text-sm leading-5 text-default-700"
            >
              <Icon
                className={
                  fulfilled ? 'mt-0.5 shrink-0 text-success' : 'mt-0.5 shrink-0 text-default-400'
                }
                size={14}
              />
              <span>{requirement.name}</span>
            </span>
          )
        })}
      </div>
    </div>
  )
}

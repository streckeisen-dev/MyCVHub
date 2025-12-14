import { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { h4 } from '@/styles/primitives.ts'
import { PasswordFormState } from '@/components/PasswordForm.tsx'

export interface PasswordRequirementsProps {
  state: PasswordFormState;
}

export function PasswordRequirements(
  props: PasswordRequirementsProps
): ReactNode {
  const { t } = useTranslation()
  const { state } = props

  const passwordRequirements = [
    {
      key: 'length',
      name: t('passwordRequirements.length', { length: '8' }),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password as string
        return pw.length >= 8
      }
    },
    {
      key: 'whitespaces',
      name: t('passwordRequirements.whitespaces'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password as string
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
        const pw = state.password as string
        return /\d/.test(pw)
      }
    },
    {
      key: 'specialChars',
      name: t('passwordRequirements.specialChars'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password as string
        return /\W/.test(pw)
      }
    },
    {
      key: 'uppercase',
      name: t('passwordRequirements.uppercase'),
      predicate: () => {
        if (state.password == null) {
          return false
        }
        const pw = state.password as string
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
        const pw = state.password as string
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
        const pw = state.password as string
        return pw === state.confirmPassword
      }
    }
  ]

  return (
    <div className="bg-default p-5 rounded-xl">
      <h4 className={h4()}>{t('account.passwordRequirements')}</h4>
      <div className="flex flex-col gap-2">
        {passwordRequirements.map((requirement) => (
          <span key={requirement.key} className={requirement.predicate() ? 'text-success' : 'text-danger' }>{requirement.name}</span>
        ))}
      </div>
    </div>
  )
}

import { ReactNode, useState } from 'react'
import { Input, InputProps } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { FaEye, FaEyeSlash } from 'react-icons/fa6'
import { ErrorMessages } from '@/types/ErrorMessages.ts'

export interface PasswordFormState {
  password: string | undefined
  confirmPassword: string | undefined
}

export type PasswordFormProps = Readonly<{
  state: PasswordFormState;
  onChange: (name: string, value: string | undefined) => void;
  errorMessages: ErrorMessages;
}>

export function PasswordInput(props: Readonly<InputProps>): ReactNode {
  const [showPassword, setShowPassword] = useState<boolean>(false)

  function toggleShowPassword() {
    setShowPassword((prev) => !prev)
  }

  return (
    <Input
      endContent={
        showPassword ? (
          <FaEyeSlash onClick={toggleShowPassword} size={25} />
        ) : (
          <FaEye onClick={toggleShowPassword} size={25} />
        )
      }
      type={showPassword ? 'text' : 'password'}
      {...props}
    />
  )
}

export function PasswordForm(props: PasswordFormProps): ReactNode {
  const { t } = useTranslation()
  const { state, errorMessages, onChange } = props

  return (
    <>
      <PasswordInput
        label={t('fields.password')}
        name="password"
        value={state.password}
        isRequired
        isInvalid={errorMessages.password != null}
        errorMessage={errorMessages.password}
        onChange={(e) => onChange('password', e.currentTarget.value)}
      />
      <PasswordInput
        label={t('fields.confirmPassword')}
        name="confirmPassword"
        value={state.confirmPassword}
        isRequired
        isInvalid={errorMessages.confirmPassword != null}
        errorMessage={errorMessages.confirmPassword}
        onChange={(e) =>
          onChange('confirmPassword', e.currentTarget.value)
        }
      />
    </>
  )
}

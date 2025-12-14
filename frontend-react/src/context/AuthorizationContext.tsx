import { AuthLevel } from '@/types/AuthLevel.ts'
import { createContext, PropsWithChildren, ReactNode, useCallback, useEffect, useState, useMemo } from 'react'
import AccountApi from '@/api/AccountApi.ts'
import { useTranslation } from 'react-i18next'
import { ThumbnailDto } from '@/types/ThumbnailDto.ts'

export interface AuthorizedUser {
  username: string
  displayName: string
  authLevel: AuthLevel
  language: string,
  hasProfile: boolean,
  thumbnail: ThumbnailDto | undefined
}

export type AccountUpdateFunction = () => void

export type LogoutFunction = () => void

export interface AuthorizationContextValue {
  user: AuthorizedUser | undefined
  handleUserUpdate: AccountUpdateFunction
  handleLogout: LogoutFunction
}

export const AuthorizationContext = createContext<AuthorizationContextValue>({
  user: undefined,
  handleUserUpdate: () => { /* empty */ },
  handleLogout: () => { /* empty */ }
})

export function AuthorizationProvider(props: PropsWithChildren): ReactNode {
  const { i18n } = useTranslation()
  const {children} = props

  const [authUser, setAuthUser] = useState<AuthorizedUser>()

  const handleUserUpdate = useCallback<AccountUpdateFunction>(() => {
    async function getAuth() {
      try {
        const auth = await AccountApi.verifyLogin(i18n.language)
        setAuthUser({
          username: auth.username,
          displayName: auth.displayName,
          authLevel: auth.authLevel,
          language: auth.language ?? i18n.language,
          hasProfile: auth.hasProfile,
          thumbnail: auth.thumbnail
        })
        if (auth.language) {
          await i18n.changeLanguage(auth.language)
        }
      } catch (_ignore) {
        setAuthUser(undefined)
      }
    }
    getAuth()
  }, [])

  useEffect(() => {
    handleUserUpdate()
  }, [handleUserUpdate])


  const handleLogout = useCallback<LogoutFunction>(() => {
    setAuthUser(undefined)
  }, [setAuthUser])

  const contextValue: AuthorizationContextValue = useMemo(() => {
    return {
      user: authUser,
      handleUserUpdate,
      handleLogout
    }
  }, [authUser, handleUserUpdate, handleLogout])

  return (
    <AuthorizationContext value={contextValue}>
      {children}
    </AuthorizationContext>
  )
}
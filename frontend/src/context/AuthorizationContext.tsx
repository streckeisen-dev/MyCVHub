import { AuthLevel } from '@/types/account/AuthLevel.ts'
import { createContext, PropsWithChildren, ReactNode, useCallback, useEffect, useState, useMemo } from 'react'
import AccountApi from '@/api/AccountApi.ts'
import { useTranslation } from 'react-i18next'
import { ThumbnailDto } from '@/types/account/ThumbnailDto.ts'

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
  isLoadingUser: boolean
  handleUserUpdate: AccountUpdateFunction
  handleLogout: LogoutFunction
}

export const AuthorizationContext = createContext<AuthorizationContextValue>({
  user: undefined,
  isLoadingUser: true,
  handleUserUpdate: () => { /* empty */ },
  handleLogout: () => { /* empty */ }
})

export function AuthorizationProvider(props: Readonly<PropsWithChildren>): ReactNode {
  const { i18n } = useTranslation()
  const {children} = props

  const [authUser, setAuthUser] = useState<AuthorizedUser>()
  const [isLoading, setIsLoading] = useState(true)

  const handleUserUpdate = useCallback<AccountUpdateFunction>(() => {
    async function getAuth() {
      setIsLoading(true)
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
      } finally {
        setIsLoading(false)
      }
    }
    getAuth()
  }, [])

  useEffect(() => {
    handleUserUpdate()
  }, [])

  const handleLogout = useCallback<LogoutFunction>(() => {
    setAuthUser(undefined)
  }, [setAuthUser])

  const contextValue: AuthorizationContextValue = useMemo(() => {
    return {
      user: authUser,
      isLoadingUser: isLoading,
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
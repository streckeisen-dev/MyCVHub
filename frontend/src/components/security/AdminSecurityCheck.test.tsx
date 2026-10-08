import { AdminSecurityCheck } from '@/components/security/AdminSecurityCheck.tsx'
import {
  AdminAuthorizationContext,
  AdminAuthorizationContextValue
} from '@/context/AdminAuthorizationContext.tsx'
import { AdminRole } from '@/types/admin/AdminRole.ts'
import { ReactNode } from 'react'
import { act } from 'react'
import { createRoot, Root } from 'react-dom/client'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, test } from 'vitest'

let root: Root | undefined
let container: HTMLDivElement | undefined

const defaultContext: AdminAuthorizationContextValue = {
  admin: undefined,
  isLoadingAdmin: false,
  handleAdminUpdate: () => { /* empty */ },
  handleAdminLogout: () => { /* empty */ }
}

function renderAdminRoute(
  context: Partial<AdminAuthorizationContextValue>,
  element: ReactNode = <AdminSecurityCheck><div>Admin content</div></AdminSecurityCheck>
) {
  container = document.createElement('div')
  document.body.append(container)
  root = createRoot(container)

  act(() => {
    root?.render(
      <AdminAuthorizationContext value={{ ...defaultContext, ...context }}>
        <MemoryRouter initialEntries={['/admin']}>
          <Routes>
            <Route path="/admin" element={element} />
            <Route path="/admin/login" element={<div>Admin login</div>} />
            <Route path="/admin/change-password" element={<div>Change admin password</div>} />
          </Routes>
        </MemoryRouter>
      </AdminAuthorizationContext>
    )
  })
}

afterEach(() => {
  act(() => {
    root?.unmount()
  })
  container?.remove()
  root = undefined
  container = undefined
})

describe('AdminSecurityCheck', () => {
  test('redirects unauthenticated admins to the admin login page', () => {
    renderAdminRoute({ admin: undefined })

    expect(container?.textContent).toContain('Admin login')
  })

  test('redirects admins with a temporary password to the password-change page', () => {
    renderAdminRoute({
      admin: {
        username: 'admin@example.com',
        role: AdminRole.ADMIN,
        mustChangePassword: true
      }
    })

    expect(container?.textContent).toContain('Change admin password')
  })

  test('allows the password-change page when temporary password access is explicitly allowed', () => {
    renderAdminRoute(
      {
        admin: {
          username: 'admin@example.com',
          role: AdminRole.ADMIN,
          mustChangePassword: true
        }
      },
      <AdminSecurityCheck allowMustChangePassword>
        <div>Password change form</div>
      </AdminSecurityCheck>
    )

    expect(container?.textContent).toContain('Password change form')
  })

  test('renders protected content for an authenticated admin with a changed password', () => {
    renderAdminRoute({
      admin: {
        username: 'admin@example.com',
        role: AdminRole.SUPER_ADMIN,
        mustChangePassword: false
      }
    })

    expect(container?.textContent).toContain('Admin content')
  })
})

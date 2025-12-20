import {
  AuthorizationContext,
  AuthorizationContextValue,
  AuthorizedUser
} from '@/context/AuthorizationContext.tsx'
import { SecurityCheck } from '@/components/security/SecurityCheck.tsx'
import { AuthLevel } from '@/types/account/AuthLevel.ts'

const CONTENT_SELECTOR = 'p[data-testid="page-content"]'

function dummyHandler() {
  // empty
}

function createUser(authLevel: AuthLevel): AuthorizedUser {
  return {
    username: 'test',
    displayName: 'Test',
    authLevel,
    language: 'en',
    hasProfile: true,
    thumbnail: undefined
  }
}

function createContextValue(user: AuthorizedUser | undefined): AuthorizationContextValue {
  return {
    user,
    handleUserUpdate: dummyHandler,
    handleLogout: dummyHandler
  }
}

function createComponent(
  context: AuthorizationContextValue,
  requiresAuth: boolean | undefined,
  minAuthLevel: AuthLevel | undefined
) {
  return (
    <AuthorizationContext value={context}>
      <SecurityCheck requiresAuth={requiresAuth} minAuthLevel={minAuthLevel}>
        <p data-testid="page-content">Content</p>
      </SecurityCheck>
    </AuthorizationContext>
  )
}

describe('SecurityCheck tests', () => {
  it('should render for unauthenticated user on non-auth page', () => {
    const context = createContextValue(undefined)

    cy.mount(createComponent(context, false, undefined))

    cy.get(CONTENT_SELECTOR).should('exist')
  })

  it('should not render for unauthenticated user on auth page', () => {
    const context = createContextValue(undefined)

    cy.mount(createComponent(context, true, undefined))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should not render for unauthenticated user on unspecified-auth page', () => {
    const context = createContextValue(undefined)

    cy.mount(createComponent(context, undefined, undefined))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should not render for incomplete user on unspecified-auth page', () => {
    const user = createUser(AuthLevel.INCOMPLETE)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, undefined))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should not render for unverified user on unspecified-auth page', () => {
    const user = createUser(AuthLevel.UNVERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, undefined))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should render for verified user on unspecified-auth page', () => {
    const user = createUser(AuthLevel.VERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, undefined))

    cy.get(CONTENT_SELECTOR).should('exist')
  })

  it('should render for incomplete user on incomplete auth page', () => {
    const user = createUser(AuthLevel.INCOMPLETE)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.INCOMPLETE))

    cy.get(CONTENT_SELECTOR).should('exist')
  })

  it('should render for unverified user on incomplete auth page', () => {
    const user = createUser(AuthLevel.UNVERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.INCOMPLETE))

    cy.get(CONTENT_SELECTOR).should('exist')
  })

  it('should render for verified user on incomplete auth page', () => {
    const user = createUser(AuthLevel.VERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.INCOMPLETE))

    cy.get(CONTENT_SELECTOR).should('exist')
  })

  it('should not render for incomplete user on unverified auth page', () => {
    const user = createUser(AuthLevel.INCOMPLETE)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.UNVERIFIED))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should render for unverified user on unverified auth page', () => {
    const user = createUser(AuthLevel.UNVERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.UNVERIFIED))
  })

  it('should render for verified user on unverified auth page', () => {
    const user = createUser(AuthLevel.VERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.UNVERIFIED))

    cy.get(CONTENT_SELECTOR).should('exist')
  })

  it('should not render for incomplete user on verified auth page', () => {
    const user = createUser(AuthLevel.INCOMPLETE)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.VERIFIED))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should not render for unverified user on verified auth page', () => {
    const user = createUser(AuthLevel.UNVERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.VERIFIED))

    cy.get(CONTENT_SELECTOR).should('not.exist')
  })

  it('should render for verified user on verified auth page', () => {
    const user = createUser(AuthLevel.VERIFIED)
    const context = createContextValue(user)

    cy.mount(createComponent(context, undefined, AuthLevel.VERIFIED))

    cy.get(CONTENT_SELECTOR).should('exist')
  })
})
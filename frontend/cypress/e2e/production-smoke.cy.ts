// Exercise the emitted production bundle, not Cypress's component dev server.
// Only API data is stubbed; scripts, styles and translations come from dist/.
describe('Production bundle', () => {
  it('loads the dashboard, navigates, reloads a deep link and saves a skill', () => {
    cy.viewport(1280, 900)
    cy.intercept('GET', '/api/auth/login/verify', {
      username: 'smoke-user', displayName: 'Smoke User', authLevel: 'VERIFIED',
      language: 'en', hasProfile: true, thumbnail: null
    }).as('auth')
    cy.intercept('GET', '/api/dashboard', {
      isVerified: true,
      profile: { experienceCount: 0, educationCount: 0, projectCount: 0, skillCount: 0 },
      applications: []
    }).as('dashboard')
    cy.intercept('GET', '/api/profile', {
      jobTitle: 'Engineer', bio: '', profilePicture: '',
      isProfilePublic: false, isEmailPublic: false, isPhonePublic: false,
      isAddressPublic: false, hideDescriptions: true,
      workExperiences: [], education: [], skills: [], projects: []
    }).as('profile')
    cy.intercept('POST', '/api/profile/skill', {
      id: 1, name: 'TypeScript', type: 'Programming', level: 0
    }).as('saveSkill')
    cy.intercept('GET', '/locales/en/translation.json').as('translations')
    cy.intercept('GET', '**/assets/*', (request) => {
      request.on('response', (response) => {
        expect(response.statusCode, request.url).to.be.oneOf([200, 304])
      })
    })

    cy.visit('/ui/dashboard', {
      onBeforeLoad(win) {
        win.localStorage.setItem('i18nextLng', 'en')
        cy.spy(win.console, 'error').as('consoleError')
      }
    })
    cy.wait('@translations')
    cy.get('@consoleError').should('not.have.been.called')
    cy.wait(['@auth', '@dashboard'])
    cy.contains('h1', 'Dashboard').should('be.visible')
    // Hashed assets prove this is a production build; stylesheet rules prove CSS loaded.
    cy.get('script[type="module"][src^="/assets/"]').should('exist')
    cy.get('link[rel="stylesheet"][href^="/assets/"]').should(($links) => {
      expect($links.length).to.be.greaterThan(0)
      for (const link of $links.toArray()) {
        expect((link as HTMLLinkElement).sheet?.cssRules.length).to.be.greaterThan(0)
      }
    })
    cy.contains('a[href="/ui/profile/edit"]', 'Edit Profile').click()
    cy.wait('@profile')
    cy.location('pathname').should('eq', '/ui/profile/edit')
    cy.contains('[role="tab"]', 'Skills').click()
    cy.location('hash').should('eq', '#skills')

    cy.reload()
    cy.wait(['@auth', '@profile'])
    cy.contains('[role="tab"]', 'Skills').should('have.attr', 'aria-selected', 'true')
    cy.contains('button', 'Add skill').click()
    cy.get('[role="dialog"]').should('be.visible').within(() => {
      cy.get('input[name="name"]').type('TypeScript')
      cy.get('input[role="combobox"]').type('Programming')
      cy.get('[data-testid="save-button"]').click()
    })
    cy.wait('@saveSkill').its('request.body').should('include', {
      name: 'TypeScript', type: 'Programming', level: 0
    })
    cy.get('[role="dialog"]').should('not.exist')
    cy.contains('TypeScript').should('be.visible')
    // Cypress's default uncaught-exception handling remains enabled.
  })
})

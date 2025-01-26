/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//

import { mount } from 'cypress/vue'
import mockI18n from '../../tests/mock/mockI18n'
import { mockVuetify } from '../../tests/mock/mockVuetify'

declare global {
  namespace Cypress {
    interface Chainable {
      mountInApp(component: any, options?: Record<string, any>): Chainable<any>
    }
  }
}

Cypress.Commands.add('mountInApp', (component, options = {}) => {
  return mount(component, {
    global: {
      plugins: [mockI18n, mockVuetify],
      ...options.global
    },
    ...options
  }).then(({ wrapper }) => {
    return cy.wrap(wrapper).as('vue')
  })
})
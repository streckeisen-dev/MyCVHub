import { defineConfig } from 'cypress'

export default defineConfig({
  component: {
    devServer: {
      framework: 'vue',
      bundler: 'vite'
    },
    reporter: 'junit',
    reporterOptions: {
      mochaFile: 'cypress/component-results.xml'
    },
    specPattern: 'cypress/component/**/*.cy.ts',
    supportFile: 'cypress/support/component.ts'
  }
})

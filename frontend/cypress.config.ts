import { defineConfig } from 'cypress'

export default defineConfig({
  component: {
    devServer: {
      framework: 'vue',
      bundler: 'vite'
    },
    reporter: 'mochawesome',
    reporterOptions: {
      reportDir: 'cypress/results',
      overwrite: true,
      html: false,
      json: true
    },
    specPattern: 'cypress/component/**/*.cy.ts',
    supportFile: 'cypress/support/component.ts'
  }
})

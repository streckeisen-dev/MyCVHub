import { defineConfig } from "cypress";

export default defineConfig({
  component: {
    devServer: {
      framework: "react",
      bundler: "vite",
    },
    reporter: 'junit',
    reporterOptions: {
      mochaFile: 'cypress/results/component-results.[suiteName].xml'
    },
    specPattern: 'cypress/components/**/*.cy.tsx',
    supportFile: 'cypress/support/component.ts'
  },
});

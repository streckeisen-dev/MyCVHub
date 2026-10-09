import { defineConfig } from "cypress";

export default defineConfig({
  component: {
    devServer: {
      framework: "react",
      bundler: "vite",
    },
    reporter: "junit",
    reporterOptions: {
      mochaFile: "cypress/results/component-results.[suiteName].xml",
    },
    specPattern: "cypress/components/**/*.cy.tsx",
    supportFile: "cypress/support/component.ts",
  },

  e2e: {
    baseUrl: "http://127.0.0.1:4173",
    specPattern: "cypress/e2e/**/*.cy.ts",
    supportFile: false,
  },
});

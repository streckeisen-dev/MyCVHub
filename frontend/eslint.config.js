import {defineConfig} from "eslint/config";
import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'
import eslintReact from '@eslint-react/eslint-plugin'

export default defineConfig({
  files: ["**/*.ts", "**/*.tsx"],
  extends: [
    eslint.configs.recommended,
    tseslint.configs.strict,
    tseslint.configs.stylisticTypeChecked,
    eslintReact.configs.strict
  ],
  languageOptions: {
    parser: tseslint.parser,
    parserOptions: {
      projectService: true,
      tsconfigRootDir: import.meta.dirname
    }
  },
  rules: {
    semi: ['error', 'never'],
    "@typescript-eslint/no-unused-vars": [
      "error",
      {
        argsIgnorePattern: "^_",
        caughtErrorsIgnorePattern: "^_",
        destructuredArrayIgnorePattern: "^_",
        varsIgnorePattern: "^_"
      }
    ],
    "comma-dangle": ["error", "never"],
    quotes: ["error", "single"]
  }
})
import path from 'node:path'
import { includeIgnoreFile } from '@eslint/compat'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const gitignorePath = path.resolve(__dirname, '.gitignore')

export default [
  includeIgnoreFile(gitignorePath),
  {
    files: ['.vue','.js','.ts'],
    root: true,
    env: {
      node: true
    },
    extends: [
      'plugin:vue/vue3-essential',
      'eslint:recommended',
      '@vue/eslint-config-typescript',
      '@vue/eslint-config-prettier/skip-formatting'
    ],
    languageOptions: {
      ecmaVersion: 'latest'
    }
  }
]
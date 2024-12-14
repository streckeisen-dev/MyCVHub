import viteConfig from './vite.config'
import { mergeConfig } from 'vite'
import { defineConfig } from 'vitest/config'

export default defineConfig(
  mergeConfig(viteConfig(), {
    test: {
      environment: 'happy-dom',
      globals: true,
      coverage: {
        provider: 'istanbul'
      },
      reporters: [
        ['junit', { suiteName: 'Frontend unit tests', outputFile: './test-results.xml' }]
      ]
    }
  })
)

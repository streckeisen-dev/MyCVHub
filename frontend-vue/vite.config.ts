import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import Vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'

const i18nPlugin = VueI18nPlugin({})

// https://vitejs.dev/config/
export default () =>
  defineConfig({
    plugins: [
      Vue(),
      vuetify(),
      i18nPlugin
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        }
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern'
        }
      }
    },
    build: {
      target: 'esnext',
      minify: 'esbuild'
    }
  })

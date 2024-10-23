import { createI18n } from 'vue-i18n'
import LanguageService from '@/services/LanguageService'

const i18n = createI18n({
  locale: LanguageService.getLanguage(),
  fallbackLocale: 'en',
  legacy: false,
  messages: {}
})

const availableLocales = Object.keys(import.meta.glob('../locales/*.json'))
  .map(file => file.match(/\.\.\/locales\/(.*)\.json$/)[1])
console.log('availableLangs', availableLocales)

for (let availableLocale of availableLocales) {
  const messages = await import(`../locales/${availableLocale}.json`)
  i18n.global.setLocaleMessage(availableLocale, messages)
}

export default i18n

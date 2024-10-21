import { createI18n } from 'vue-i18n'
import LanguageService from '@/services/LanguageService'
import globalLocales from '@/locales/GlobalLocales'

const i18n = createI18n({
  locale: LanguageService.getLanguage(),
  fallbackLocale: 'en',
  legacy: false,
  messages: globalLocales
})

export default i18n

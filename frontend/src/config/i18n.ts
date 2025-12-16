import i18n from 'i18next'
import Backend from 'i18next-http-backend'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'

i18n
  .use(Backend)
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'en',
    debug: false,
    detection: {
      convertDetectedLanguage: (lang: string) => {
        if (lang.includes('-')) {
          return lang.split('-')[0]
        } else if (lang.includes('_')) {
          return lang.split('_')[0]
        } else {
          return lang
        }
      }
    }
  })
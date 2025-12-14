import i18n from 'i18next'
import Backend from 'i18next-http-backend'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'

/*const resources: Resource = {}
//const localeDateFormats: LocaleDateFormat = {}
const langModules = import.meta.glob('../locales/*.json', { eager: true })
Object.keys(langModules).forEach((filePath) => {
  const lang = filePath.replace('../locales/', '').replace('.json', '')
  resources[lang] = langModules[filePath] as ResourceLanguage
})
console.log(resources)*/

/*Object.keys(resources).forEach((lang) => {
  localeDateFormats[lang] = {
    monthAndYear: {
      month: 'short',
      year: 'numeric'
    },
    simpleDate: {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }
  }
})*/

i18n
  .use(Backend)
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'en',
    debug: true,
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

export default i18n
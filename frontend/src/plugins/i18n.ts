import { createI18n } from 'vue-i18n'
import LanguageService from '@/services/LanguageService'
import * as vuetifyLocale from 'vuetify/locale'

type NestedMessage = {
  [key: string]: string | NestedMessage
}

type Messages = {
  [locale: string]: NestedMessage
}

type DateFormat = {
  [key: string]: string
}

type LocaleDateFormat = {
  [locale: string]: { [name: string]: DateFormat }
}

const messages: Messages = {}
const localeDateFormats: LocaleDateFormat = {}
const langModules = import.meta.glob('../locales/*.json', { eager: true })
Object.keys(langModules).forEach((filePath) => {
  const lang = filePath.replace('../locales/', '').replace('.json', '')
  const vuetifyMessages = (vuetifyLocale as Messages)[lang]
  messages[lang] = {
    ...(langModules[filePath] as NestedMessage),
    $vuetify: vuetifyMessages
  }
})

Object.keys(messages).forEach((lang) => {
  localeDateFormats[lang] = {
    monthAndYear: {
      month: 'short',
      year: 'numeric'
    },
    shortDate: {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }
  }
})

const i18n = createI18n({
  locale: LanguageService.getLanguage(),
  fallbackLocale: 'en',
  legacy: false,
  messages,
  datetimeFormats: localeDateFormats
})

export default i18n

import { toBoolean } from '@/services/BooleanHelper.ts'
import { LocaleInstance } from 'vuetify/framework'

export const LANGUAGE_KEY = 'language'
export const ACCOUNT_LANGUAGE_KEY = 'accountLanguage'

function getLanguage(): string {
  return localStorage.getItem(LANGUAGE_KEY) ?? navigator.language.split('-')[0]
}

function setLanguage(language: string, locale: LocaleInstance, isAccountLanguage: boolean = false) {
  const lang = language ?? getLanguage()
  localStorage.setItem(LANGUAGE_KEY, lang)
  locale.current.value = lang
  localStorage.setItem(ACCOUNT_LANGUAGE_KEY, isAccountLanguage.toString())
}

function isAccountLanguage(): boolean {
  return toBoolean(localStorage.getItem(ACCOUNT_LANGUAGE_KEY)) || false
}

export default {
  getLanguage,
  setLanguage,
  isAccountLanguage
}

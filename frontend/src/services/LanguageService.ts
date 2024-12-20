export const LANGUAGE_KEY = 'language'

function getLanguage(): string {
  return localStorage.getItem(LANGUAGE_KEY) || navigator.language.split('-')[0]
}

function setLanguage(language: string) {
  localStorage.setItem(LANGUAGE_KEY, language)
}

export default {
  getLanguage,
  setLanguage
}

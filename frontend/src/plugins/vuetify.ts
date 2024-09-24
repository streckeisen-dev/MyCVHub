import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

import { createVuetify } from 'vuetify'

function getMediaPreference() {
  const hasDarkPreference = window.matchMedia('(prefers-color-scheme: dark)').matches
  if (hasDarkPreference) {
    return 'dark'
  } else {
    return 'light'
  }
}

const vuetify = createVuetify({
  theme: {
    defaultTheme: getMediaPreference(),
    variations: {
      colors: ['nav', 'secondary', 'background', 'border', 'error'],
      lighten: 0,
      darken: 0
    },
    themes: {
      dark: {
        dark: true,
        colors: {
          primary: '#151b23',
          'btn-primary': '#56aff5',
          surface: '#010409',
          background: '#010409'
        },
        variables: {
          'border-color': '61,68,77',
          'border-opacity': '0.9'
        }
      },
      light: {
        dark: false,
        colors: {
          nav: '#f6f8fa',
          background: '#ffffff',
          border: '#d1d9e0'
        }
      }
    }
  }
})

export default vuetify

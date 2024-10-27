import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

import { createVuetify } from 'vuetify'
import { createVueI18nAdapter } from 'vuetify/locale/adapters/vue-i18n'
import i18n from '@/plugins/i18n'
import { useI18n } from 'vue-i18n'

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
					primary: '#4169E1', // Blue accent from the logo
					secondary: '#8A8A8A', // Lighter gray for softer contrast
					background: '#010409', // Dark background from logo
					surface: '#0D1117', // Dark gray for cards/surfaces
					onPrimary: '#FFFFFF', // Text color on primary elements
					onBackground: '#C9D1D9' // Light gray text on dark background
				},
				variables: {
					'border-color': '61,68,77',
					'border-opacity': '0.9'
				}
			},
			light: {
				dark: false,
				colors: {
					primary: '#4169E1', // Blue accent from the logo
					secondary: '#202020', // Neutral dark gray for contrast
					background: '#FFFFFF', // Background color for light mode
					surface: '#F4F4F4', // Slightly off-white for surfaces/cards
					onPrimary: '#FFFFFF', // Text color on primary elements
					onBackground: '#202020' // Text color for main content
				}
			},
			profile: {
				dark: false,
				colors: {
					nav: '#f6f8fa',
					background: '#ffffff',
					border: '#d1d9e0',
					'btn-primary': '#56aff5',
					'btn-background': '#5660f5'
				}
			}
		}
	},
	locale: {
		adapter: createVueI18nAdapter({ i18n, useI18n })
	}
})

export default vuetify
